package com.coillighting.udder.effect.woven;

public enum CueFadeStateEnum {

    // TODO: rename invisible to start?
    INVISIBLE("invisible"),
    FADE_IN("fade_in"),
    FULL("full");

    private String value;

    private CueFadeStateEnum(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }

};
