package com.coillighting.udder.effect;

import com.coillighting.udder.geometry.ControlQuad;

/** Convey public parameters to and from TextureEffect instances.
 *  This class serves as a JSON mapping target for Boon.
 */
public class TextureEffectState {

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

    /** If true, automatically stretch and squeeze the image over the rig.
     *  If false, distort the image map according to the four corners in
     *  controlQuad.
     */
    protected boolean automatic = true;

    /** If automatic is true, then vary the stretching and squeezing motion
     *  this often or faster. (We randomly vary step time from 1 ms at the
     *  fast end to maxTempoMillis at the slow end, and each corner steps
     *  indepdently, so on average you'll see some change in your show
     *  about 4x this often, although many such random changes are subtle
     *  or even invisible.) A good value is 18000 (18 seconds).
     *  Send <=0 for this value to be ignored, retaining the current tempo.
     */
    protected int maxTempoMillis = 0;

    /** If automatic is false, then don't heed maxTempoMillis; simply obey
     *  the mapping specified here. See Stretch.stretchXY for implementation.
     *  Set to null to keep the current controlQuad.
     */
    protected ControlQuad controlQuad = null;

    public TextureEffectState(String fileName, boolean automatic,
                                int maxTempoMillis, ControlQuad controlQuad)
    {
        this.filename = filename;
        this.automatic = automatic;
        this.maxTempoMillis = maxTempoMillis;
        this.controlQuad = controlQuad;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean getAutomatic() {
        return automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    public int getMaxTempoMillis() {
        return maxTempoMillis;
    }

    public void setMaxTempoMillis(int maxTempoMillis) {
        this.maxTempoMillis = maxTempoMillis;
    }

    public ControlQuad getControlQuad() {
        return controlQuad;
    }

    /** Send the unit square to reset to a 1:1 mapping. */
    public void setControlQuad(ControlQuad controlQuad) {
        this.controlQuad = controlQuad;
    }

    public String toString() {
        return "TextureEffectState(\"" + filename + "\", automatic="
            + automatic + ", maxTempoMillis=" + maxTempoMillis + ", "
            + controlQuad + ")";
    }
}