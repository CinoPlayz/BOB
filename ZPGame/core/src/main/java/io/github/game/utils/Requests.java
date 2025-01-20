package io.github.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.ByteBuffer;

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
}
