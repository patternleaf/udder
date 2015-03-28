package com.coillighting.udder.effect;

import com.coillighting.udder.geometry.TriangularSequence;
import static com.coillighting.udder.util.LogUtil.log;

/** TODO DOC */
public class BloomEffect extends EffectBase {

    public Class getStateClass() {
        return BloomEffectState.class;
    }

    public Object getState() {
        return null; // TODO BloomEffectState
    }

    public void setState(Object state) throws ClassCastException {
        log("TODO: BlooomEffect.setState()");
    }

}