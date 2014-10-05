package com.coillighting.udder.woven;

public enum CueEnum {

    BLACKOUT("blackout"),
    CURTAIN("curtain"),
    WARP("warp"),
    WEFT("weft"),
    FINALE("finale"),
    FADEOUT("fadeout");

    private String value;

    private CueEnum(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }

};
