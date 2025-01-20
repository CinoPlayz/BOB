package io.github.game.utils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoClientConnect {
    public static MongoDatabase database = getMongoDatabase();
    private static MongoDatabase getMongoDatabase(){
        String uri = "mongodb://localhost:27017/";
        MongoDatabase mongoDatabase = null;

        // Construct a ServerApi instance using the ServerApi.builder() method
        ServerApi serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build();

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(uri))
            .serverApi(serverApi)
            .build();

        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("ZP");
            try {
                // Send a ping to confirm a successful connection
                Bson command = new BsonDocument("ping", new BsonInt64(1));
                Document commandResult = database.runCommand(command);
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
                mongoDatabase = database;
            } catch (MongoException me) {
                System.err.println(me);
            }
        }

        return mongoDatabase;
    }
}
