#![allow(non_snake_case)]

use chrono::{DateTime, Utc};
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

impl PassengerData{
    pub fn new() -> PassengerData{
        return PassengerData{
            userRequestDateTime: Utc::now(),
            coordinatesOfUserRequest: Coordinates::new(),
            guessedOccupancyRate: 0,
            realOccupancyRate: 0,
            routeId: String::new(),
            postedByUserId: String::new(),
        }
    }
}

impl Coordinates{
    pub fn new() -> Coordinates{
        return Coordinates { lat: 0.0, lng: 0.0 }
    }
}