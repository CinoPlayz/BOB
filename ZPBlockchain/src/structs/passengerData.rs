#![allow(non_snake_case)]

use chrono::{DateTime, TimeZone, Utc};
use mpi::{traits::{Collection, Equivalence}, Error};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct PassengerData{    
    pub userRequestDateTime: DateTime<Utc>,
    pub coordinatesOfUserRequest: Coordinates,
    pub guessedOccupancyRate: i32,
    pub realOccupancyRate: i32,
    pub routeId: String,
    pub postedByUserId: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Coordinates{
    pub lat: f32,
    pub lng: f32
}

#[derive(Debug, Clone, Equivalence)]
pub struct PassengerDataMPI{    
    pub userRequestDateTime: i64,
    pub coordinatesOfUserRequest: CoordinatesMPI,
    pub guessedOccupancyRate: i32,
    pub realOccupancyRate: i32,
    pub routeId: [u8; 24],
    pub postedByUserId: [u8; 24],
}

#[derive(Debug, Clone, Equivalence)]
pub struct CoordinatesMPI{
    pub lat: f32,
    pub lng: f32
}

impl PassengerData{
    pub fn new() -> PassengerData{
        return PassengerData{
            userRequestDateTime: Utc::now(),
            coordinatesOfUserRequest: Coordinates::new(),
            guessedOccupancyRate: 0,
            realOccupancyRate: 0,
            routeId: String::from("000000000000000000000000"),
            postedByUserId: String::from("000000000000000000000000"),
        }
    }

    pub fn toPassengerDataMPI(&self) -> Result<PassengerDataMPI, String>{
        let routeIdInBytes = self.routeId.as_bytes();
        let routeIdArray: Result<[u8; 24], _> = routeIdInBytes.try_into();
        if routeIdArray.is_err(){
            return Err(String::from("routeId is not right type"));
        }
        let roureId = routeIdArray.unwrap();

        let postedByIdInBytes = self.routeId.as_bytes();
        let postedByIdArray: Result<[u8; 24], _> = postedByIdInBytes.try_into();
        if postedByIdArray.is_err(){
            return Err(String::from("postedByUserId is not right type"));
        }
        let postedById = postedByIdArray.unwrap();

        return Ok(PassengerDataMPI{
            userRequestDateTime: self.userRequestDateTime.timestamp_millis(),
            coordinatesOfUserRequest: self.coordinatesOfUserRequest.toCoordinatesMPI(),
            guessedOccupancyRate: self.guessedOccupancyRate,
            realOccupancyRate: self.realOccupancyRate,
            routeId: roureId,
            postedByUserId: postedById,
        });
    }
}

impl Coordinates{
    pub fn new() -> Coordinates{
        return Coordinates { lat: 0.0, lng: 0.0 }
    }

    pub fn toCoordinatesMPI(&self) -> CoordinatesMPI{
        return CoordinatesMPI { lat: self.lat, lng: self.lng }
    }
}

impl PassengerDataMPI{
    pub fn toPassengerData(&self) -> PassengerData{
        let strRouteId = std::str::from_utf8(&self.routeId).unwrap();
        let routeId = String::from(strRouteId);

        let strUserId = std::str::from_utf8(&self.postedByUserId).unwrap();
        let postedByUserId = String::from(strUserId);

        return PassengerData{
            userRequestDateTime: DateTime::from_timestamp_millis(self.userRequestDateTime).unwrap(),
            coordinatesOfUserRequest: self.coordinatesOfUserRequest.toCoordinates(),
            guessedOccupancyRate: self.guessedOccupancyRate,
            realOccupancyRate: self.realOccupancyRate,
            routeId,
            postedByUserId
        };
    }
}

impl CoordinatesMPI{
    pub fn toCoordinates(&self) -> Coordinates{
        return Coordinates { lat: self.lat, lng: self.lng }
    }
}