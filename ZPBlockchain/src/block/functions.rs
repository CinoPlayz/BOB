#![allow(non_snake_case)]
use crate::structs::block::Block;

fn isValidHash(hash: String, block: &Block) -> bool{
    let blockHash = block.getHash();
    return blockHash == hash;
}

fn isValidBlockInChain(previousBlock: Option<&Block>, nowBlock: &Block) -> bool{
    match previousBlock {
        None => return true,
        Some(previousBlock) => {
            let previousBlockHash = previousBlock.getHash();
            let previousBlockHashInNow = &nowBlock.lastBlockHash;

            return previousBlockHash == previousBlockHashInNow.to_owned();            
        }        
    }
}

fn isValidBlockChain(blockchain: &Vec<Block>) -> bool{
    for i in 0..blockchain.len()  {
        let isValid ;
        if i == 0 {
            isValid = isValidBlockInChain(None, &blockchain[i])
        }
        else {
            isValid = isValidBlockInChain(Some(&blockchain[i]), &blockchain[i])
        }

        if !isValid {
            return false;
        }
    }
    return true;
}