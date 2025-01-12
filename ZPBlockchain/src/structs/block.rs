use base64ct::{Base64, Encoding};
use chrono::Utc;
use serde::{Deserialize, Serialize};
use sha2::{Sha256, Digest};

use crate::structs::passengerData::PassengerData;

#[derive(Serialize, Deserialize, Debug)]
pub struct Block{
    pub index: u32,
    pub data: PassengerData,
    pub timeStamp: i64, //In milli seconds
    pub lastBlockHash: String,
    pub difficulty: u32,
    pub nonce: u32,
    pub hash: String    
}

#[derive(Serialize, Deserialize, Debug)]
pub struct BlockWhileMining{
    pub index: u32,
    pub data: PassengerData,
    pub timeStamp: i64, //In milli seconds
    pub lastBlockHash: String,
    pub difficulty: u32,
    pub nonce: u32
}

impl Block{
    pub fn new() -> Block{
        return Block{
            index: 0,
            data: PassengerData::new(),
            timeStamp: Utc::now().timestamp(),
            lastBlockHash: String::new(),
            difficulty: 0,
            nonce: 0,
            hash: String::new(),
        };
    }

    pub fn getHash(&self) -> String{
        return self.hash.clone();
    }
}

impl BlockWhileMining {
    pub fn getHashString(&self) -> String {
        let hash = Sha256::digest(self.getBinary());
        let hashInString = hex::encode(hash);
        return hashInString;
    }

    pub fn getHashBinary(&self) -> Vec<u8>{
        return self.getBinary();
    }

    fn getBinary(&self) -> Vec<u8> {
        let jsonString = self.getJson();
        return jsonString.as_bytes().to_owned();
    }

    fn getJson(&self) -> String{
        let jsonString = serde_json::to_string(self);
        return jsonString.unwrap_or("".to_string());
    }
}