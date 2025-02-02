#![allow(non_snake_case)]
pub mod structs;
pub mod block;
pub mod mining;

use std::{
    env, fs::OpenOptions, io::Write, process::exit, sync::{ mpsc::{ self, Receiver, Sender }, Arc, Mutex }, thread::{ self, available_parallelism, ScopedJoinHandle }
};

use bson::oid::ObjectId;
use chrono::Utc;
use mongodb::{bson::doc, options::ClientOptions, Client, Collection, error::Error};
use mpi::{
    point_to_point::Status,
    request::{ LocalScope, Request },
    traits::{ Communicator, Destination, Source },
};
use structs::{
    block::Block,
    passengerData::{ Coordinates, PassengerData, PassengerDataMongoDB },
    processTransmitionData::{ BroadcastNewBlockData, FoundHash },
    tags::Tags,
};

fn main() {
    //Read args
    let args: Vec<String> = env::args().collect();
    let mut numOfThreads: usize = 0;
    let mut clientOption: Option<Client> = None;
    let argsLen = args.len();

    for i in 1..argsLen{
        if args[i] == "-c" && argsLen >= i+1 {
            let mut clientOptions = ClientOptions::parse(args[i+1].clone()).run().expect("Wrong connection string");
            clientOptions.app_name = Some(String::from("ZPBlockchain"));
            let client = Client::with_options(clientOptions).expect("Cannot create Client");
            clientOption = Some(client);
        }

        if args[i] == "-n" && argsLen >= i+1 {            
            numOfThreads = args[i+1].parse::<usize>().expect("Number of threads not a number");
        }

        if args[i] == "-h" {
            println!("Avaliable flags:");
            println!("-c  -- Connection string to mongodb");
            println!("-n  -- Number of threads used for mining");
            println!("-h  -- Help");
            println!();
        }
    }  

    doWorkMPI(numOfThreads, clientOption);
}

fn doWorkMPI(mut numOfThreads: usize, clientOption: Option<Client>){
    let universe = mpi::initialize().unwrap();
    let world = universe.world();
    let size = world.size();
    let rank = world.rank();
    let root_process = 0;

    mpi::request::scope(|scope| {
        //Execute if root process /
        if rank == root_process {
            let mut blockchain: Vec<Block> = Vec::new();
            let mut indexOfBlock = 0;
            let mut hashOfLastBlock =
                "0000000000000000000000000000000000000000000000000000000000000000".to_owned();
            let mut difficulty = 3;
            let mut canChangeBlock = true;
            let mut hasRequestedBlock = false;
            let mut hasFoundHash = false;
            let mut foundHash = FoundHash::new();            
            let mut data: PassengerData = PassengerData::new();
            let mut timeStamp: i64 = Utc::now().timestamp_millis() + 30000;
            let mut lastDateOfCheck = Utc::now();
            let mut docs: Vec<PassengerDataMongoDB> = Vec::new();
            let mut alreadyRemoved: Vec<ObjectId> = Vec::new();

            loop {
                if canChangeBlock {
                    canChangeBlock = false;
                    let mut foundDoc = false;

                    //Get data for new block from mongodb database if avaiable
                    if clientOption.is_some(){
                        let client = clientOption.clone().unwrap();
                        let db = client.database("ZP");
                        let collection: Collection<PassengerDataMongoDB> = db.collection("passengers");
                        let findDocument =
                            doc! {
                                "$or": [
                                    {"createdAt": {"$lte": lastDateOfCheck}},
                                    {"updatedAt": {"$lte": lastDateOfCheck}}
                                ],                     
                        };    

                        let result = collection.find(findDocument).run();                 
                        
                        match result {
                            Err(e) => {
                                println!("Rank 0: Error when getting from mongodb: {}", e.to_string())
                            },
                            Ok(cursor) => {
                                let results: Vec<Result<PassengerDataMongoDB, Error>> = cursor.collect();                                
                                
                                for result in results{

                                    //Check if block is not already queued for sending to miners
                                    let resUnwraped = result.unwrap();
                                    let mut found = false;
                                    for id in &alreadyRemoved {
                                        if *id == resUnwraped._id{
                                            found = true;
                                            break;
                                        }
                                    }

                                    if !found {
                                        for doc in &docs{
                                            if doc._id == resUnwraped._id{
                                                found = true;
                                                break;
                                            }
                                        }
                                    }

                                    if !found{
                                        docs.push(resUnwraped);
                                    }                                    
                                }
                            }                            
                        }
                        lastDateOfCheck = Utc::now();

                        //Check if there are any documents to send them to miners
                        if docs.len() != 0 {
                            data = docs[0].toPassengerData();
                            alreadyRemoved.push(docs[0]._id);
                            docs.remove(0);
                            foundDoc = true;
                            println!("Count: {}", docs.len())
                        }
                    }

                    //Fill data if there were no new documents
                    if !foundDoc{
                        //Generate data for block
                        data = PassengerData {
                            userRequestDateTime: Utc::now(),
                            coordinatesOfUserRequest: Coordinates { lat: 2.0, lng: 3.0 },
                            guessedOccupancyRate: 20,
                            realOccupancyRate: 30,
                            routeId: "000000000000000000000000".to_owned(),
                            postedByUserId: "000000000000000000000000".to_owned(),
                        };
                    }                    
                    timeStamp = Utc::now().timestamp_millis() + 30000;

                    //Checks if there has been found the right hash for block
                    if blockchain.len() != 0 {
                        indexOfBlock += 1;
                        hashOfLastBlock = blockchain.last().unwrap().hash.clone();
                    }

                    //Difficulty adjusment each 10 blocks
                    if blockchain.len() % 10 == 0 && blockchain.len() != 0 {
                        let adjecmentBlock = blockchain.last().unwrap();
                        let expectedTime: i64 = 10000 * 10; //10s for 1 block, so 10s * 10 for 10 blocks

                        let index10Old: u32 = (blockchain.len() as u32) - 10;
                        let realTime: i64 =
                            adjecmentBlock.timeStamp - blockchain[index10Old as usize].timeStamp;

                        if realTime < expectedTime / 2 {
                            difficulty += 1;
                        } else if realTime > expectedTime * 2 {
                            difficulty -= 1;
                        }

                        println!("New difficulty: {}", difficulty);
                    }
                }
                
                //Build data of block that will be send to miners
                let dataPassengerMPI = data.toPassengerDataMPI().unwrap();
                let hashOfLastBlockMPI: [u8; 64] = hashOfLastBlock
                    .as_bytes()
                    .try_into()
                    .unwrap();

                let broadcastData = BroadcastNewBlockData {
                    passangerDataMPI: dataPassengerMPI,
                    timeStamp,
                    indexOfBlock,
                    hashOfLastBlock: hashOfLastBlockMPI,
                    difficulty,
                };                   

               

                let mut requestedRankBlockData = 0;
                let mut requestedRankFoundHash = 0;

                while !hasRequestedBlock && !hasFoundHash {

                    //Get if someone requested block data
                    let anyMessagesRequestBlock = world.any_process().immediate_probe_with_tag(Tags::RequestBlock as i32);
                    match anyMessagesRequestBlock {
                        Some(_) => {
                            let msgBlockData: (bool, Status) = world.any_process().receive_with_tag(Tags::RequestBlock as i32);
                            hasRequestedBlock = true;
                            requestedRankBlockData = msgBlockData.1.source_rank();
                        },
                        None => {},
                    }                    

                    //Get if someone found hash
                    let anyMessagesFoundHash = world.any_process().immediate_probe_with_tag(Tags::FoundHash as i32);
                    match anyMessagesFoundHash {
                        Some(_) => {
                            let msgFoundHash: (FoundHash, Status) = world.any_process().receive_with_tag(Tags::FoundHash as i32);
                            hasFoundHash = true;
                            foundHash = msgFoundHash.0;
                            requestedRankFoundHash = msgFoundHash.1.source_rank();
                        },
                        None => {},
                    }
                }


                //Checks if someone requested block data and then send data
                if hasRequestedBlock {
                    world.process_at_rank(requestedRankBlockData).send_with_tag(&broadcastData, Tags::NewBlock as i32);
                    hasRequestedBlock = false;                    
                }

                //Checks if someone found hash 
                if hasFoundHash {
                    hasFoundHash = false;

                    if foundHash.indexOfBlock == indexOfBlock{
                        canChangeBlock = true;

                        let nounce = foundHash.nounce;
                        let strHashOfBlock = std::str::from_utf8(&foundHash.hash).unwrap();
                        let hashOfBlock = String::from(strHashOfBlock);
    
                        let newBlockToAdd = Block {
                            index: indexOfBlock,
                            data: data.clone(),
                            timeStamp,
                            lastBlockHash: hashOfLastBlock,
                            difficulty,
                            nonce: nounce,
                            hash: hashOfBlock.clone(),
                        };
    
                        blockchain.push(newBlockToAdd);

                        println!("Rank 0: New Blockchain size: {}", blockchain.len());

                        if !block::functions::isValidBlockChain(&blockchain) {
                            println!("{:?}", blockchain);
                            exit(-1);
                        } else {
                            //Send stop signal for miners
                            let mut vecOfSendProcesses: Vec<
                                Request<'_, bool, &LocalScope<'_>>
                            > = Vec::new();
                            for i in 1..size {
                                let sendRequest = world
                                    .process_at_rank(i)
                                    .immediate_send_with_tag(scope, &true, Tags::ExitMining as i32);
                                vecOfSendProcesses.push(sendRequest);
                            }
    
                            for sendRequest in vecOfSendProcesses {
                                sendRequest.wait_without_status();
                            }
                            hashOfLastBlock = hashOfBlock;

                            //Write blockchain to file
                            let mut file = OpenOptions::new().create(true).read(true).write(true).open("blockchain.json").unwrap();
                            file.write_all(serde_json::to_string(&blockchain).unwrap().as_bytes()).unwrap();
                        }                        
                    }
                }               
               
            }
            //Execute in any procees other then root
        } else {
            loop {
                //Request block data from root process
                world.process_at_rank(root_process).send_with_tag(&true, Tags::RequestBlock as i32);

                //Recieve block data from root process
                let msgNewBlockTulip: (BroadcastNewBlockData, _) = world
                    .process_at_rank(root_process)
                    .receive_with_tag(Tags::NewBlock as i32);
                let msgNewBlock = msgNewBlockTulip.0;

                //println!("Rank {} got new message", rank);

                //Inicialization of variables
                let mut foundByProcess = false;
                let timeStamp = msgNewBlock.timeStamp;
                let data = msgNewBlock.passangerDataMPI.toPassengerData();
                let indexOfBlock = msgNewBlock.indexOfBlock;
                let difficulty = msgNewBlock.difficulty;
                let strHashOfLastBlock = std::str::from_utf8(&msgNewBlock.hashOfLastBlock).unwrap();
                let hashOfLastBlock = String::from(strHashOfLastBlock);
                if numOfThreads == 0 {
                    numOfThreads = available_parallelism().unwrap().get();
                }

                let numberInMPI: u32 = (rank as u32) - 1;
                let numberOfMPIProcessors: u32 = (size as u32) - 1;
                let noncesPerProcessor = u32::MAX / numberOfMPIProcessors;

                let nounceStartProcessor = noncesPerProcessor * numberInMPI;
                //let nounceEndProcessor = noncesPerProcessor * (numberInMPI + 1);

                let nouncePerThread = noncesPerProcessor / (numOfThreads as u32);

                //Check if block timestamp is in the future for 1 minute
                let nowTimeStamp = Utc::now().timestamp_millis();
                if timeStamp > nowTimeStamp && timeStamp <= nowTimeStamp + 60000 {
                    //Mutex for returned block with right hash
                    let refReturnedBlock = Arc::new(Mutex::new(Block::new()));
                    let refReturnedBlockClone = &refReturnedBlock.clone();

                    //Mutex for passanger data that will be in the block
                    let dataRef = Arc::new(Mutex::new(data));
                    let dateRefClone = &dataRef.clone();

                    //Mutex for hash of last block
                    let hashOfLastBlockRef = Arc::new(Mutex::new(hashOfLastBlock.clone()));
                    let hashOfLastBlockRefClone = &hashOfLastBlockRef.clone();

                    //Mutex for vector of senders to threads (threads recieve that right hash has been found)
                    let isFoundTxVec: Vec<Sender<bool>> = Vec::new();
                    let isFoundRef = Arc::new(Mutex::new(isFoundTxVec));
                    let isFoundRefClone = &isFoundRef.clone();

                    //Mutex for a thread that found right hash to send a massage to main thread
                    let (txThreadHasFound, rxThreadHasFound): (
                        Sender<bool>,
                        Receiver<bool>,
                    ) = mpsc::channel();
                    let threadHasFoundRef = Arc::new(Mutex::new(txThreadHasFound));
                    let threadHasFoundRefClone = &threadHasFoundRef.clone();

                    //Register a scope to know variables won't be droped before all threads are finished
                    thread::scope(|scopeLocal| {
                        //Vec to save handlers of threads
                        let mut handles: Vec<ScopedJoinHandle<()>> = Vec::new();

                        for i in 0..numOfThreads {
                            let nounceStart = nounceStartProcessor + nouncePerThread * (i as u32);
                            let nounceEnd =
                                nounceStartProcessor + nouncePerThread * ((i as u32) + 1);

                            //Spawn a thread in current scope
                            let handle = scopeLocal.spawn(move || {
                                //Register mpsc channel for threads to recieve if right hash has been found
                                let (txIsFound, rxIsFound): (
                                    Sender<bool>,
                                    Receiver<bool>,
                                ) = mpsc::channel();

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

                                        let mut lockedRefBlock = refReturnedBlockClone
                                            .lock()
                                            .unwrap();
                                        *lockedRefBlock = block;
                                    }
                                    None => {/*println!("Found nothing")*/}
                                }
                            });

                            handles.push(handle);
                        }

                        //Loop while right hash hasnt been found
                        loop {
                            //Has recieved message
                            let probeExit = world
                                .process_at_rank(root_process)
                                .immediate_probe_with_tag(Tags::ExitMining as i32);

                            match probeExit {
                                Some(_) => {
                                    println!("Rank {}: In closing by process", rank);
                                    foundByProcess = true;
                                    let _: (bool, Status) = world
                                        .process_at_rank(root_process)
                                        .receive_with_tag(Tags::ExitMining as i32);
                                    break;
                                }
                                None => {}
                            }

                            //println!("Rank: {} Looping here", rank);

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

                        //Send data to root process only if it wasn't already found by another process
                        if !foundByProcess {
                            let blockLock = refReturnedBlock.lock().unwrap();
                            let blockToSend = blockLock.clone();

                            let hashInBytes = blockToSend.hash.as_bytes();
                            let hashArray: [u8; 64] = hashInBytes.try_into().unwrap();

                            let foundHash = FoundHash {
                                indexOfBlock,
                                hash: hashArray,
                                nounce: blockToSend.nonce,
                            };

                            println!("Rank {}: found hash for block {}", rank, indexOfBlock);

                            world
                                .process_at_rank(root_process)
                                .send_with_tag(&foundHash, Tags::FoundHash as i32);
                        }
                    });
                }
            }
        }
    });
}
