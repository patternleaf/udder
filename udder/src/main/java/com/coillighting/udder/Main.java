package com.coillighting.udder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.boon.json.JsonFactory;

import com.coillighting.udder.infrastructure.*;
import com.coillighting.udder.mix.Mixer;
import com.coillighting.udder.model.Device;
import com.coillighting.udder.scene.DairyScene;
import com.coillighting.udder.util.FileUtil;

import static com.coillighting.udder.util.LogUtil.log;

/** Application entrypoint for the Boulder Dairy show, which has double
 * duty as Udder's example application.
 *
 * Loads the Dairy show and starts a new Udder lighting server.
 * Configuration details are specified in a *.properties file.
 */
public class Main {
    // TODO rename to DairyShow

    static final String ARG_CREATE_OPC_LAYOUT = "--create-opc-layout";

    /** Main entrypoint. See printUsage for args. */
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.start(args);
    }

    /** Parse the config file and assemble the infrastructure for a show.
     * If the user has requested an OPC layout JSON export, just generated
     * the layout file and quit. Otherwise, start the server threads and
     * wait for requests.
     */
    public void start(String [] args) throws DeviceAddressException, FileNotFoundException, IOException {
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

        if(args.length == 1 || args.length == 3) {
            // Could convert from .io to .nio here.
            configPath = args[0];
            fconfig = new File(configPath);
            if(!fconfig.exists()) {
                this.die("File not found: " + configPath);
            } else if(fconfig.isDirectory()) {
                this.die("Not a regular file: " + configPath);
            } else if(args.length == 3) {
                String flag = args[1];
                if(flag.equals(ARG_CREATE_OPC_LAYOUT)) {
                    layoutPath = args[2];
                    flayout = new File(layoutPath);
                    if(flayout.exists()) {
                        this.die("OPC layout file already exists at '" + layoutPath
                        + "'. Delete it yourself if you're ready to part with it.");
                    }
                } else {
                    this.die("Unrecognized command line parameter: " + flag);
                }
            }
        } else {
            this.die(null);
        }

        System.out.println("Using config " + configPath);

        Properties prop = new Properties();
        prop.load(new FileInputStream(configPath));

        String patchSheetPath = this.translateSeparators(
                this.getMandatoryProperty(prop, configPath, DairyProperties.PATCH_SHEET));

        String udderAddr = this.getMandatoryProperty(prop, configPath, DairyProperties.UDDER_ADDRESS);
        Integer udderPort = this.parseInteger(
            this.getMandatoryProperty(prop, configPath, DairyProperties.UDDER_PORT));

        // Might be null. That's okay.
        Integer frameDelayMillis = this.parseInteger(
                prop.getProperty(DairyProperties.UDDER_FRAME_DELAY));

        String opcServer1Addr = this.getMandatoryProperty(prop, configPath, DairyProperties.OPC_SERVER1_HOST);
        Integer opcServer1Port = this.parseInteger(
                this.getMandatoryProperty(prop, configPath, DairyProperties.OPC_SERVER1_PORT));

        String opcServer2Addr = prop.getProperty(DairyProperties.OPC_SERVER2_HOST);
        Integer opcServer2Port = this.parseInteger(
            prop.getProperty(DairyProperties.OPC_SERVER2_PORT));

        if(opcServer2Addr == null && opcServer2Port != null) {
            this.die("If you specify " + DairyProperties.OPC_SERVER2_HOST
                + ", you must also provide " + DairyProperties.OPC_SERVER2_PORT + ".");
        }

        if(opcServer2Port == null && opcServer2Addr != null) {
            this.die("If you specify " + DairyProperties.OPC_SERVER2_PORT
                    + ", you must also provide " + DairyProperties.OPC_SERVER2_HOST + ".");
        }

        ArrayList<SocketAddress> opcServerAddresses = new ArrayList<SocketAddress>(2);
        opcServerAddresses.add(new SocketAddress(opcServer1Addr, opcServer1Port));

        if(opcServer2Addr != null && opcServer2Port != null) {
            opcServerAddresses.add(new SocketAddress(opcServer2Addr, opcServer2Port));
        }

        PatchSheet patchSheet = PatchSheet.parsePatchSheet(patchSheetPath);
        Mixer mixer = DairyScene.create(patchSheet.getModelSpaceDevices());

        ServicePipeline pipeline = new ServicePipeline(
                mixer,
                patchSheet.getDeviceAddressMap(),
                frameDelayMillis,
                new SocketAddress(udderAddr, udderPort),
                opcServerAddresses);

        // Config parsing and validation is now complete.

        if(layoutPath != null) {
            String layoutJson = JsonFactory.toJson(
                    OpcLayoutPoint.createOpcLayoutPoints(patchSheet));
            FileUtil.stringToFile(layoutPath, layoutJson);
            log("------------------------------------------------------------\n"
                + "Wrote OPC JSON layout to " + layoutPath + '\n'
                + "Now restart any process (such as gl_server) that uses this layout file.\n"
                + "Also restart the Udder server if you changed the patch sheet.");
            System.exit(0);
        } else {
            pipeline.start();
        }
    }

    protected String getMandatoryProperty(Properties prop, String configPath, String key) {
        String value = prop.getProperty(key);
        if(value == null) {
            this.die("The properties file '" + configPath
                    + "' does not contain the mandatory " + key + " key.");
        }
        return value;
    }

    /** Translate generic forward-slash path separators into the system
     * specific separator, in case we're running on Windows.
     */
    protected String translateSeparators(String path) {
        if(path == null) {
            return null;
        }
        return path.replace('/', File.separatorChar);
    }

    /** Parse a String as an Integer, returning null if the string is null. */
    protected Integer parseInteger(String s) {
        if(s == null) {
            return null;
        }
        return new Integer(s);
    }

    protected void die(String errorMessage) {
        this.printUsage(errorMessage);
        System.exit(1);
    }

    protected void printUsage(String errorMessage) {
        if(errorMessage != null) {
            System.out.println(errorMessage);
        }
        System.out.println(
            "Usage: java com.coillighting.udder.Main path/to/scene.properties ["
            + ARG_CREATE_OPC_LAYOUT + " path/to/opc_layout.json]\n"
            + "    (Specify an OPC layout filename to generate a new layout\n"
            + "    file for consumption by openpixelcontrol's gl_server.)");
    }
}

/** Keys for the dairy.properties file. */
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

    /** Attempt to mix down and render a new frame approximately this often.
     * Target framerate (fps) = 1000 / udder.frameDelayMillis.
     *
     * The default frame delay is 10ms, for 100 fps: very smooth.
     *
     * Whether Udder actually achieves this framerate depends on the available
     * processing power of your host computer and the complexity of the visible
     * elements in your scene.
     *
     * If your CPU is maxxed out, then you need to drop this value.
     * If you have plenty of CPU to spare, try raising it and see if you can
     * notice this effect. A higher frame rate is more noticeable when either
     * the audience or the light fixture is in motion.
     */
    public static final String UDDER_FRAME_DELAY = "udder.frameDelayMillis";

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
