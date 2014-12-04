package com.coillighting.udder.effect.woven;

public enum CueEnum {

    BLACKOUT("blackout"),
    // CURTAIN("curtain"), cut 12/4 per crew consensus
    WARP("warp"),
    WEFT("weft"),
    // FINALE("finale"), cut (ditto)
    FADEOUT("fadeout");

    private String value;

    private CueEnum(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }

};
