#![allow(non_snake_case)]
use mpi::traits::Equivalence;

use super::passengerData::PassengerDataMPI;

#[derive(Debug, Clone, Equivalence)]
pub struct BroadcastNewBlockData{
    pub passangerDataMPI: PassengerDataMPI,
    pub timeStamp: i64,
    pub indexOfBlock: u32,
    pub hashOfLastBlock: [u8; 64],
    pub difficulty: u32
}

#[derive(Debug, Clone, Equivalence)]
pub struct FoundHash{
    pub indexOfBlock: u32,
    pub nounce: u32,
    pub hash: [u8; 64]
}

impl FoundHash{
    pub fn new() -> FoundHash{
        let hashInBytes = "0000000000000000000000000000000000000000000000000000000000000000".as_bytes();
        let hashArray: [u8; 64] = hashInBytes.try_into().unwrap();
        return FoundHash { indexOfBlock: 0, nounce: 0, hash: hashArray}
    }
}
