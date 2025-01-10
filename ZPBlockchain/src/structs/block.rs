use base64ct::{Base64, Encoding};
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

impl Block {
    pub fn getHash(&self) -> String {
        let hash = Sha256::digest(self.getBinary());
        let base64Hash = Base64::encode_string(&hash);
        return base64Hash;
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