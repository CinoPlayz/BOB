#![allow(non_snake_case)]
pub mod structs;
pub mod block;
pub mod mining;

use std::{
    sync::{ mpsc::{self, Receiver, Sender}, Arc, Mutex },
    thread::{ self, available_parallelism, ScopedJoinHandle },
};

use chrono::Utc;
use structs::{ block::Block, passengerData::{ Coordinates, PassengerData } };

fn main() {
    let data = PassengerData {
        userRequestDateTime: Utc::now(),
        coordinatesOfUserRequest: Coordinates { lat: 2.0, lng: 3.0 },
        guessedOccupancyRate: 20,
        realOccupancyRate: 30,
        routeId: "kldfgsdf".to_owned(),
        postedByUserId: "fgji4odt".to_owned(),
    };

    let indexOfBlock = 0;
    let hashOfLastBlock = "0".to_owned();
    let difficulty = 6;
    let timeStamp = Utc::now().timestamp_millis() + 60000;

    let numberInMPI: u32 = 0;
    let numberOfMPIProcessors: u32 = 2;
    let noncesPerProcessor = u32::MAX / numberOfMPIProcessors;

    let nounceStartProcessor = noncesPerProcessor * numberInMPI;
    //let nounceEndProcessor = noncesPerProcessor * (numberInMPI + 1);

    let numOfThreads = available_parallelism().unwrap().get();
    let nouncePerThread = noncesPerProcessor / (numOfThreads as u32);

    //Mutex for returned block with right hash
    let refReturnedBlock = Arc::new(Mutex::new(Block::new()));
    let refReturnedBlockClone = &refReturnedBlock.clone();    

    //Mutex for passanger data that will be in the block
    let dataRef = Arc::new(Mutex::new(data));
    let dateRefClone = &dataRef.clone();

    //Mutex for hash of last block
    let hashOfLastBlockRef = Arc::new(Mutex::new(hashOfLastBlock));
    let hashOfLastBlockRefClone = &hashOfLastBlockRef.clone();

    //Mutex for vector of senders to threads (threads recieve that right hash has been found)
    let isFoundTxVec: Vec<Sender<bool>> = Vec::new();
    let isFoundRef = Arc::new(Mutex::new(isFoundTxVec));  
    let isFoundRefClone = &isFoundRef.clone();

    //Mutex for a thread that found right hash to send a massage to main thread
    let (txThreadHasFound, rxThreadHasFound): (Sender<bool>, Receiver<bool>) = mpsc::channel(); 
    let threadHasFoundRef = Arc::new(Mutex::new(txThreadHasFound));
    let threadHasFoundRefClone = &threadHasFoundRef.clone();

    //Register a scope to know variables won't be droped before all threads are finished
    thread::scope(|scopeLocal|{
        //Vec to save handlers of threads
        let mut handles: Vec<ScopedJoinHandle<()>> = Vec::new();        
        
        for i in 0..numOfThreads {
            let nounceStart = nounceStartProcessor + nouncePerThread * (i as u32);
            let nounceEnd = nounceStartProcessor + nouncePerThread * ((i as u32) + 1);
            
            //Spawn a thread in current scope
            let handle = scopeLocal.spawn(move || {

                //Register mpsc channel for threads to recieve if right hash has been found
                let (txIsFound, rxIsFound): (Sender<bool>, Receiver<bool>) = mpsc::channel();
                
                //Write sender to threads into vector of senders to threads
                let mut isFoundTxLock = isFoundRefClone.lock().unwrap();
                isFoundTxLock.push(txIsFound);
                std::mem::drop(isFoundTxLock);

                //Start mining
                let returnedBlock = mining::functions::startMining(
                    &dateRefClone,
                    indexOfBlock,
                    hashOfLastBlockRefClone,
                    difficulty,
                    timeStamp,
                    nounceStart,
                    nounceEnd,
                    rxIsFound
                );

                //Get result of mining
                match returnedBlock {
                    Some(block) => {
                        let hasFoundLock = threadHasFoundRefClone.lock().unwrap();
                        let _ = hasFoundLock.send(true);

                        let mut lockedRefBlock = refReturnedBlockClone.lock().unwrap();
                        *lockedRefBlock = block;
                    }
                    None => {
                        println!("Found nothing")
                    },
                }
            });
    
            handles.push(handle);
        }

        //Loop while right hash hasnt been found
        loop{
            //Check if from MPI

            //Check if thread has found right hash by receving a message from him
            let hasFoundResult = rxThreadHasFound.try_recv();
            if hasFoundResult.is_ok() {
                break;
            }           
        }

        //Sents all threads that a right hash has been found
        let isFoundTxLock = isFoundRefClone.lock().unwrap();
        let isFoundVec = isFoundTxLock.clone();
        for sender in isFoundVec {
            let _ = sender.send(true);
        }
        std::mem::drop(isFoundTxLock);
                
        //Wait for all threads to finish
        for handle in handles {
            let _ = handle.join();
        }
    });

    

    let refre = refReturnedBlock.lock().unwrap();
    println!("{:?}", refre);
}
