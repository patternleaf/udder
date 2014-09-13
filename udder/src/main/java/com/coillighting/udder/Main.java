package com.coillighting.udder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.boon.json.JsonFactory;

import com.coillighting.udder.DairyScene;
import com.coillighting.udder.PatchElement;
import com.coillighting.udder.ServicePipeline;


/** Application entrypoint.
 *  Start up a new Udder lighting server. Load the scene for the Boulder Dairy.
 */
public class Main {

    /** Arguments are currently ignored. TODO: configuration. */
    public static void main(String[] args) throws Exception {
        File f = null;
        String configPath = null; // e.g. "conf/patch_sheet.json"
        if(args.length == 1) {
            // FIXME: use nio & Paths, not ancient io, for path validation?
            configPath = args[0];
            f = new File(configPath);
            if(!f.exists()) {
                Main.die("File not found: " + configPath);
            } else if(f.isDirectory()) {
                Main.die("Not a regular file: " + configPath);
            }
        } else {
            Main.die(null);
        }
        System.err.println("Using config " + configPath);
        ServicePipeline pipeline = new ServicePipeline(
            DairyScene.create(
                Main.createDevicesFromJSONFile(configPath)));
        pipeline.start();
    }

    public static void die(String errorMessage) {
        Main.printUsage(errorMessage);
        System.exit(1);
    }

    public static void printUsage(String errorMessage) {
        if(errorMessage != null) {
            System.err.println(errorMessage);
        }
        System.err.println(
            "Usage: java com.coillighting.udder.Main [path/to/config.json]");
    }

    public static List<PatchElement> createDevicesFromJSONFile(String filename)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(filename));
        String json = new String(encoded, StandardCharsets.UTF_8);
        return JsonFactory.fromJsonArray(json, PatchElement.class);
    }

}
