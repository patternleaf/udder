package com.coillighting.udder.effect.woven;

public enum CueFadeStateEnum {

    START("start"),
    RUNNING("running"),
    END("end");

    private String value;

    private CueFadeStateEnum(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }

};
