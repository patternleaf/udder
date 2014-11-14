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

import com.coillighting.udder.infrastructure.OpcLayoutPoint;
import com.coillighting.udder.infrastructure.PatchElement;
import com.coillighting.udder.infrastructure.PatchSheet;
import com.coillighting.udder.infrastructure.ServicePipeline;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.scene.DairyScene;

// TODO drive the animation when people aren't hitting it with requests
// look into Timer, TimerTask, and this stuff from JDK 5+:
// ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
// es.schedule(new Runnable(){
//     @Override
//     public void run() {
//         //RSS checking
//     }
// }, 10, TimeUnit.MINUTES);

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
            // TODO: convert from .io to .nio?
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

        System.out.println("Using config " + configPath);
        PatchSheet patchSheet = Main.createDevices(configPath);

        if(layoutPath != null) {
            String layoutJson = JsonFactory.toJson(
                Main.createOpcLayoutPointsFromDevices(patchSheet));
            Main.stringToFile(layoutPath, layoutJson);
            System.err.println("Dumped OPC JSON layout to " + layoutPath);
        }

        ServicePipeline pipeline = new ServicePipeline(
            DairyScene.create(patchSheet.getModelSpaceDevices()),
            patchSheet.getDeviceAddressMap());
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
            "Usage: java com.coillighting.udder.Main path/to/config.json [opc_layout.json]\n"
            +"    (Specify an OPC layout filename to generate a new layout\n"
            +"    file for consumption by openpixelcontrol's gl_server.)");
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

    /** Parse the JSON patch sheet as a list of Devices. */
    protected static PatchSheet createDevices(String configPath)
            throws IllegalArgumentException, IOException {

        byte[] encoded = Files.readAllBytes(Paths.get(configPath));
        String json = new String(encoded, StandardCharsets.UTF_8);
        List<PatchElement> patchElements = JsonFactory.fromJsonArray(
            Main.fileToString(configPath), PatchElement.class);

        // TODO REF? Why Arraylist instead of array? (Same below.)
        List<Device> devices = new ArrayList<Device>(patchElements.size());
        for(PatchElement pe: patchElements) {
            devices.add(pe.toDevice());
        }
        return new PatchSheet(devices);
    }

    /** Return a list associating OPC address with spatial coordinates.
     *  The Open Pixel Control's gl_server consumes this list and presents
     *  a very simple 3D view of your show, which you can use as a monitor.
     *  See https://github.com/patternleaf/archway for a fine example of how to
     *  create a much snazzier 3D monitor in your browser.
     */
    protected static List<OpcLayoutPoint> createOpcLayoutPointsFromDevices(PatchSheet patchSheet) {
        // We occasionally skip scaling when debugging.
        final boolean autoscale = true;

        // Shrink the layout, which at the Dairy arrived in inches, to fit the
        // limited viewport of the OPC gl model.
        final double glViewportScale = 3.0;

        final double [] origin = {0.0, 0.0, 0.0};

        final List<Device> devices = patchSheet.getModelSpaceDevices();
        final int[] addrMap = patchSheet.getDeviceAddressMap();
        ArrayList<OpcLayoutPoint> points = new ArrayList<OpcLayoutPoint>(addrMap.length);

        // Walk the devices in OPC address order. Position a point per address.
        for(int deviceIndex: addrMap) {
            double[] pt;
            if(deviceIndex < 0) {
                // If a device for an OPC address is not patched, just put that
                // address's pixel on the origin where it won't cause trouble.
                pt = origin.clone();
            } else {
                pt = devices.get(deviceIndex).getPoint().clone();

                // Flip the z-axis to match the Dairy show's model space to
                // openpixelcontrol's gl_server window.
                pt[2] *= -1;
            }
            points.add(new OpcLayoutPoint(pt));
        }

        if(autoscale) {
            double modelScale = 0.0;

            // Compute how far we need to scale down the gl_server model to
            // fit the unit cube.
            for(OpcLayoutPoint opcPoint: points) {
                double[] pt = opcPoint.getPoint();
                for(int i=0; i<pt.length; i++) {
                    if(Math.abs(pt[i]) > Math.abs(modelScale)) {
                        modelScale = pt[i];
                    }
                }
            }

            // Scale the output OPC layout to fit the model onscreen.
            for(OpcLayoutPoint opcPoint: points) {
                opcPoint.scale(glViewportScale / modelScale);
            }
        }
        return points;
    }

}
