#![allow(non_snake_case)]
use std::sync::{mpsc::Receiver, Arc, Mutex};

use chrono::Utc;

use crate::structs::{ block::{ Block, BlockWhileMining }, passengerData::PassengerData };

fn doesHashHave0(hash: &String, zerosCount: u32) -> bool {
    let mut chars = hash.chars();

    for _ in 0..zerosCount {
        if chars.nth(0).unwrap() != '0' {
            return false;
        }
    }
    return true;
}

pub fn startMining(
    dataRef: &Arc<Mutex<PassengerData>>,
    indexOfBlock: u32,
    hashOfLastBlockRef: &Arc<Mutex<std::string::String>>,
    difficulty: u32,
    timeStamp: i64,
    startNonce: u32,
    endNounce: u32,
    rxIsFound: Receiver<bool>
) -> Option<Block> {
    //println!("Start: {}, End: {}", startNonce, endNounce);
    let dataLock = dataRef.lock().unwrap();
    let data = dataLock.clone();
    std::mem::drop(dataLock);

    let hashLock = hashOfLastBlockRef.lock().unwrap();
    let hashLast = hashLock.clone();
    std::mem::drop(hashLock);

    for nonce in startNonce..endNounce {
        let foundResult = rxIsFound.try_recv();
        let found = foundResult.is_ok();

        if found {
            break;
        }

        let block = BlockWhileMining {
            index: indexOfBlock,
            data: data.clone(),
            timeStamp, //In future
            lastBlockHash: hashLast.clone(),
            difficulty,
            nonce,
        };

        let hash = block.getHashString();
        if doesHashHave0(&hash, difficulty) {
            return Some(Block {
                index: indexOfBlock,
                data: data,
                timeStamp,
                lastBlockHash: hashLast,
                difficulty,
                nonce,
                hash: hash,
            });
        }
    }
    return None;
}
