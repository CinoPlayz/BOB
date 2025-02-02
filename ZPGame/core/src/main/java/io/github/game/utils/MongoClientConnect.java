package io.github.game.utils;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

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
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

public class MongoClientConnect {
    public static MongoDatabase database;
    private static MongoClient client;

   static {
        String uri = "mongodb://localhost:27017/";

       CodecRegistry pojoCodecRegistry = fromRegistries(
           MongoClientSettings.getDefaultCodecRegistry(),
           fromProviders(PojoCodecProvider.builder().automatic(true).build(),
               PojoCodecProvider.builder().register(Coordinates.class).conventions(Conventions.DEFAULT_CONVENTIONS).build())
       );

        // Construct a ServerApi instance using the ServerApi.builder() method
        ServerApi serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build();

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(uri))
            .serverApi(serverApi)
            .codecRegistry(pojoCodecRegistry)
            .build();

        // Create a new client and connect to the server
        client = MongoClients.create(settings);
        database = client.getDatabase("ZP");
        try {
           // Send a ping to confirm a successful connection
           Bson command = new BsonDocument("ping", new BsonInt64(1));
           Document commandResult = database.runCommand(command);
           System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
        } catch (MongoException me) {
           System.err.println(me);
        }
    }
}
