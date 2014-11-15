package com.coillighting.udder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.boon.json.JsonFactory;

import com.coillighting.udder.infrastructure.OpcLayoutPoint;
import com.coillighting.udder.infrastructure.PatchElement;
import com.coillighting.udder.infrastructure.PatchSheet;
import com.coillighting.udder.infrastructure.ServicePipeline;
import com.coillighting.udder.infrastructure.SocketAddress;
import com.coillighting.udder.mix.Mixer;
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

    // TODO convert these static methods to class methods, and minimize the
    // static main method.

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

        Properties prop = new Properties();
        prop.load(new FileInputStream(configPath));

        String patchSheetPath = Main.translateSeparators(
            Main.getMandatoryProperty(prop, configPath, DairyProperties.PATCH_SHEET));

        String udderAddr = Main.getMandatoryProperty(prop, configPath, DairyProperties.UDDER_ADDRESS);
        Integer udderPort = Main.parseInteger(
            Main.getMandatoryProperty(prop, configPath, DairyProperties.UDDER_PORT));

        String opcServer1Addr = Main.getMandatoryProperty(prop, configPath, DairyProperties.OPC_SERVER1_HOST);
        Integer opcServer1Port = Main.parseInteger(
            Main.getMandatoryProperty(prop, configPath, DairyProperties.OPC_SERVER1_PORT));

        String opcServer2Addr = prop.getProperty(DairyProperties.OPC_SERVER2_HOST);
        Integer opcServer2Port = Main.parseInteger(
            prop.getProperty(DairyProperties.OPC_SERVER2_PORT));

        if(opcServer2Addr == null && opcServer2Port != null) {
            Main.die("If you specify " + DairyProperties.OPC_SERVER2_HOST
                + ", you must also provide " + DairyProperties.OPC_SERVER2_PORT + ".");
        }

        if(opcServer2Port == null && opcServer2Addr != null) {
            Main.die("If you specify " + DairyProperties.OPC_SERVER2_PORT
                    + ", you must also provide " + DairyProperties.OPC_SERVER2_HOST + ".");
        }

        ArrayList<SocketAddress> opcServerAddresses = new ArrayList<SocketAddress>(2);
        opcServerAddresses.add(new SocketAddress(opcServer1Addr, opcServer1Port));

        if(opcServer2Addr != null && opcServer2Port != null) {
            opcServerAddresses.add(new SocketAddress(opcServer2Addr, opcServer2Port));
        }

        PatchSheet patchSheet = Main.createDevices(patchSheetPath);
        Mixer mixer = DairyScene.create(patchSheet.getModelSpaceDevices());

        ServicePipeline pipeline = new ServicePipeline(
                mixer,
                patchSheet.getDeviceAddressMap(),
                new SocketAddress(udderAddr, udderPort),
                opcServerAddresses);

        if(layoutPath != null) {
            String layoutJson = JsonFactory.toJson(
                    Main.createOpcLayoutPointsFromDevices(patchSheet));
            Main.stringToFile(layoutPath, layoutJson);
            System.out.println("Dumped OPC JSON layout to " + layoutPath);
        }

        pipeline.start();
    }

    protected static String getMandatoryProperty(Properties prop, String configPath, String key) {
        String value = prop.getProperty(key);
        if(value == null) {
            Main.die("The properties file '" + configPath
                    + "' does not contain the mandatory " + key + " key.");
        }
        return value;
    }

    /** Translate generic forward-slash path separators into the system
     * specific separator, in case we're running on Windows.
     */
    protected static String translateSeparators(String path) {
        if(path == null) {
            return null;
        }
        return path.replace('/', File.separatorChar);
    }

    protected static Integer parseInteger(String s) {
        if(s == null) {
            return null;
        }
        return new Integer(s);
    }

    protected static void die(String errorMessage) {
        Main.printUsage(errorMessage);
        System.exit(1);
    }

    protected static void printUsage(String errorMessage) {
        if(errorMessage != null) {
            System.out.println(errorMessage);
        }
        System.out.println(
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

class DairyProperties {

    /** The list of Devices (lights) and their locations in space.
     * Mandatory. Example: "conf/patch_sheet.json". Forward slashes are
     * converted to the system-specific File.separator.
     */
    public static final String PATCH_SHEET = "patchSheet";

    /** The Udder webserver binds to this local address.
     * Mandatory. Example: "127.0.0.1"
     */
    public static final String UDDER_ADDRESS = "udder.address";

    /** The Udder webserver listens on this port.
     * Mandatory. Example: "8080".
     */
    public static final String UDDER_PORT = "udder.port";

    /** The primary downstream Open Pixel Control Server is at this address.
     * By convention, this is the server that drives your devices.
     * Mandatory. Example: "127.0.0.1".
     */
    public static final String OPC_SERVER1_HOST = "opcServer1.host";

    /** The primary downstream Open Pixel Control Server listens on this port.
     * See OPC_SERVER1_ADDRESS for details. Mandatory. Example: "7890".
     */
    public static final String OPC_SERVER1_PORT = "opcServer1.port";

    /** The secondary downstream Open Pixel Control Server is at this address.
     * By convention, this is your visualizer or monitor, but it could be a
     * second OPC server that drives more lights. If both servers are
     * specified, then both servers receive copies of the same frames, however
     * frame synchronization is necessarily imperfect.
     * Optional. Example: "127.0.0.1".
     */
    public static final String OPC_SERVER2_HOST = "opcServer2.host";

    /** The secondary downstream Open Pixel Control Server listens on this port.
     * See OPC_SERVER2_ADDRESS for details. Optional. Example: "8888".
     */
    public static final String OPC_SERVER2_PORT = "opcServer2.port";

}
