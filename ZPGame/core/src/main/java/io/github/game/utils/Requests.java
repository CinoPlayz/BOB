package io.github.game.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Requests {
    private static final Geolocation CENTER_GEOLOCATION = new Geolocation(46.55, 14.96);
    public static RequestTiles requestTiles(OrthographicCamera camera){
        Texture[] mapTiles = null;
        ZoomXY beginTile = null;
        RequestTiles requestTiles = null;

        try {
            int zoomOntoMap = (int) (Constants.ZOOM * camera.zoom);
            System.out.println(zoomOntoMap);
            ZoomXY centerTile = MapRasterTiles.getTileNumber(
                CENTER_GEOLOCATION.lat,
                CENTER_GEOLOCATION.lng,
                zoomOntoMap
            );

            boolean hasFoundInCache = false;
            for (int i = 0; i < Constants.NUM_TILES; i++){

            }

            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);

            /*for(int i = 0; i < mapTiles.length; i++){
                FileHandle file = Gdx.files.local("tiles_map/zoom_"+ zoomOntoMap + "_iter_" + i+".png");
                Texture tile = mapTiles[i];
                Pixmap pixmap = tile.getTextureData().consumePixmap();
                ByteBuffer byteBuf = pixmap.getPixels();
                while (byteBuf.hasRemaining()){
                    byte b = byteBuf.get();
                    file.writeBytes(new byte[]{b}, true);
                }
            }
            System.out.println("Textures count " + mapTiles.length);*/

            beginTile = new ZoomXY(
                Constants.ZOOM,
                centerTile.x - ((Constants.NUM_TILES - 1) / 2),
                centerTile.y - ((Constants.NUM_TILES - 1) / 2)
            );

            requestTiles = new RequestTiles();
            requestTiles.tiles = mapTiles;
            requestTiles.beginTile = beginTile;
        } catch (IOException e) {
            e.printStackTrace();
        }


        return requestTiles;
    }

    public static List<TrainLocHistory> getTrainList(LocalDateTime startDate, LocalDateTime endDate){
        Bson filter = new Document("$gte", startDate).append("$lt", endDate);

        MongoCollection<TrainLocHistory> collection = MongoClientConnect.database.getCollection("trainlochistories", TrainLocHistory.class);
        FindIterable<TrainLocHistory> findIter = collection.find(new Document("timeOfRequest", filter));
        List<TrainLocHistory> docs = new ArrayList<>();
        findIter.into(docs);
        System.out.println(docs.size());

        return new ArrayList<>(docs);
    }
}
