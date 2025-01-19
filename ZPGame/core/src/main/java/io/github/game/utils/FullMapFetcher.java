package io.github.game.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FullMapFetcher {

    public static void fetchFullMap(String url, String outputFile) throws IOException {
        // Prenesi podatke z URL-ja
        ByteArrayOutputStream bis = fetchUrlData(new URL(url));

        // Zapiši podatke v datoteko
        writeBytesToFile(outputFile, bis.toByteArray());
    }

    private static ByteArrayOutputStream fetchUrlData(URL url) throws IOException {
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = is.read(buffer)) > 0) {
            bis.write(buffer, 0, bytesRead);
        }

        return bis;
    }

    private static void writeBytesToFile(String outputFile, byte[] bytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(bytes);
        }
    }
}
