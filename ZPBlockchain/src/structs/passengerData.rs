#![allow(non_snake_case)]

use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug)]
pub struct PassengerData{    
    userRequestDateTime: DateTime<Utc>,
    coordinatesOfUserRequest: Coordinates,
    guessedOccupancyRate: i32,
    realOccupancyRate: i32,
    routeId: String,
    postedByUserId: String,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct Coordinates{
    lat: f32,
    lng: f32
}