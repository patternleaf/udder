package com.coillighting.udder;


public class Command {
    private Integer value = null;

    public Command(Integer value) {
        this.value = value;
    }

    public String toString() {
        return "Command{value=" + this.value + "}";
    }

    public Integer getValue() {
        return this.value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
