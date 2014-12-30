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
    protected String filename;

    /** Roll the image loop horizontally this often. 0 ms = stationary. */
    protected Integer xPeriodMillis;

    /** Roll the image loop vertically this often. 0 ms = stationary. */
    protected Integer yPeriodMillis;

    /** Roll the image loop horizontally this often. 0 ms = stationary. */
    protected Double xRotate;

    /** Roll the image loop vertically this often. 0 ms = stationary. */
    protected Double yRotate;

    // TODO figure out how to vary rate without changing offset. might
    // need to use Doubles and Integers rather than ints and doubles.

    public RollEffectState(String fileName, Integer xPeriodMillis, Integer yPeriodMillis,
                           Double xRotate, Double yRotate)
    {
        this.filename = fileName;
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

    public Integer getXPeriodMillis() {
        return xPeriodMillis;
    }

    public void setXPeriodMillis(Integer xPeriodMillis) {
        this.xPeriodMillis = xPeriodMillis;
    }

    public Integer getYPeriodMillis() {
        return yPeriodMillis;
    }

    public void setYPeriodMillis(Integer yPeriodMillis) {
        this.yPeriodMillis = yPeriodMillis;
    }

    public Double getXRotate() {
        return xRotate;
    }

    public void setXRotate(Double xRotate) {
        this.xRotate = xRotate;
    }

    public Double getYRotate() {
        return yRotate;
    }

    public void setYRotate(Double yRotate) {
        this.yRotate = yRotate;
    }

}