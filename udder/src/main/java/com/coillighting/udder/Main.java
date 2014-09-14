package com.coillighting.udder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.boon.json.JsonFactory;

import com.coillighting.udder.DairyScene;
import com.coillighting.udder.Device;
import com.coillighting.udder.PatchElement;
import com.coillighting.udder.ServicePipeline;


/** Application entrypoint.
 *  Start up a new Udder lighting server. Load the scene for the Boulder Dairy.
 */
public class Main {

    /** Arguments are currently ignored. TODO: configuration. */
    public static void main(String[] args) throws Exception {

        // Required path to the patch sheet listing devices.
        // e.g. "conf/patch_sheet.json"
        String configPath = null;
        File fconfig = null;

        // Optional path to a new file where we should dump a stripped-down
        // version of the patch sheet for the consumption of the OPC OpenGL
        // visualizer.
        // e.g. "conf/patch_sheet.json"
        String layoutPath = null; // Optional. e.g. "opc_layout.json"
        File flayout = null;

        if(args.length == 1 || args.length == 2) {
            // FIXME: convert from .io to .nio?
            configPath = args[0];
            fconfig = new File(configPath);
            if(!fconfig.exists()) {
                Main.die("File not found: " + configPath);
            } else if(fconfig.isDirectory()) {
                Main.die("Not a regular file: " + configPath);
            } else if(args.length == 2) {
                layoutPath = args[1];
                flayout = new File(layoutPath);
                if(flayout.exists()) {
                    Main.die("Layout file already exists: " + layoutPath);
                }
            }
        } else {
            Main.die(null);
        }

        System.err.println("Using config " + configPath);
        List<Device> devices = Main.createDevices(configPath);

        if(layoutPath != null) {
            String layoutJson = JsonFactory.toJson(
                Main.createOpcLayoutPointsFromDevices(devices));
            Main.stringToFile(layoutPath, layoutJson);
            System.err.println("Dumped OPC JSON layout to " + layoutPath);
        }

        ServicePipeline pipeline = new ServicePipeline(
            DairyScene.create(devices));
        pipeline.start();
    }

    protected static void die(String errorMessage) {
        Main.printUsage(errorMessage);
        System.exit(1);
    }

    protected static void printUsage(String errorMessage) {
        if(errorMessage != null) {
            System.err.println(errorMessage);
        }
        System.err.println(
            "Usage: java com.coillighting.udder.Main path/to/config.json [opc_layout.json]");
    }

    // TODO: move static utils into their own class
    protected static void stringToFile(String path, String payload)
            throws FileNotFoundException {

        PrintWriter out = new PrintWriter(path);
        out.println(payload);
        out.close();
    }

    protected static String fileToString(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    /** Parse the JSON patch sheet as a list of Devices. The 'gate' properties
     *  from the patch sheet are interpreted as Device.group values. (FIXME)
     */
    protected static List<Device> createDevices(String configPath)
            throws IOException {

        byte[] encoded = Files.readAllBytes(Paths.get(configPath));
        String json = new String(encoded, StandardCharsets.UTF_8);
        List<PatchElement> patchElements = JsonFactory.fromJsonArray(
            Main.fileToString(configPath), PatchElement.class);

        // TODO sort out address mapping. Currently all addresses are in model space.
        List<Device> devices = new ArrayList(patchElements.size());
        int addr = 0;
        for(PatchElement pe: patchElements) {
            Device device = pe.toDevice(addr);
            System.err.println(device); //TEMP
            devices.add(device);
            ++addr;
        }
        return devices;
    }

    protected static List<OpcLayoutPoint> createOpcLayoutPointsFromDevices(List<Device> devices) {
        ArrayList<OpcLayoutPoint> points = new ArrayList<OpcLayoutPoint>(devices.size());
        for (Device device: devices) {
            points.add(new OpcLayoutPoint(device.getPoint()));
        }
        return points;
    }

}
