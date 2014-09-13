package com.coillighting.udder;

/** A deserialized command to the udder server. Normally this command originates
 *  from an external client, often a browser. Commands do not know or care how
 *  they were wrapped for transport. They represent only their payload.
 *
 *  Currently the payload is just a numeric placeholder value. (TODO)
 */
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
