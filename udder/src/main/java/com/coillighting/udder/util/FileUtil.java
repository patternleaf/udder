package com.coillighting.udder.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {

    public static void stringToFile(String path, String payload)
            throws FileNotFoundException {

        PrintWriter out = new PrintWriter(path);
        out.println(payload);
        out.close();
    }

    public static String fileToString(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

}