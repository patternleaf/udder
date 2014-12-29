package com.coillighting.udder.effect;

/** Convey public parameters to and from RollEffect instances.
 *  This class serves as a JSON mapping target for Boon.
 */
public class RollEffectState {

    /** Map this texture onto the rig. Normally we store these in
     * udder/udder/images, so a typical fileName looks like
     * "images/my_file_name.png". Several common RGB and ARGB image types are
     * theoretically supported, however we have tested only PNG and JPG. Consult
     * the Java API docs for ImageIO.read() for details.
     * TextureEffect will reload the file when this value changes.
     * Send null or the empty string for this value to be ignored, so you keep
     * showing the current file.
     */
    protected String filename = null;

    /** Roll the image loop horizontally this often. 0 ms = stationary. */
    protected int xPeriodMillis = 0;

    /** Roll the image loop vertically this often. 0 ms = stationary. */
    protected int yPeriodMillis = 0;

    /** Roll the image loop horizontally this often. 0 ms = stationary. */
    protected double xRotate = 0.0;

    /** Roll the image loop vertically this often. 0 ms = stationary. */
    protected double yRotate = 0.0;

    // TODO figure out how to vary rate without changing offset. might
    // need to use Doubles and Integers rather than ints and doubles.

    public RollEffectState(String fileName, int xPeriodMillis, int yPeriodMillis,
                           double xRotate, double yRotate)
    {
        this.filename = filename;
        this.xPeriodMillis = xPeriodMillis;
        this.yPeriodMillis = yPeriodMillis;
        this.xRotate = xRotate;
        this.yRotate = yRotate;
    }

    public String toString() {
        return "RollEffectState(\"" + filename + "\", xPeriodMillis="
            + xPeriodMillis + ", yPeriodMillis=" + yPeriodMillis
            + ", xRotate=" + xRotate + ", yRotate=" + yRotate + ")";
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getXPeriodMillis() {
        return xPeriodMillis;
    }

    public void setXPeriodMillis(int xPeriodMillis) {
        this.xPeriodMillis = xPeriodMillis;
    }

    public int getYPeriodMillis() {
        return yPeriodMillis;
    }

    public void setYPeriodMillis(int yPeriodMillis) {
        this.yPeriodMillis = yPeriodMillis;
    }

    public double getXRotate() {
        return xRotate;
    }

    public void setXRotate(double xRotate) {
        this.xRotate = xRotate;
    }

    public double getYRotate() {
        return yRotate;
    }

    public void setYRotate(double yRotate) {
        this.yRotate = yRotate;
    }

}