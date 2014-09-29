package com.coillighting.udder;

/** A deserialized command to the udder server. Normally this command originates
 *  from an external client, often a browser. Commands do not know or care how
 *  they were wrapped for transport. They represent only their payload.
 *
 *  The command's payload is an arbitrary object (or null).
 */
public class Command {

    private Object value = null;
    private Integer destination = null;

    public Command(Object value, Integer destination) {
        this.value = value;
        this.destination = destination;
    }

    public String toString() {
        return "Command{value=" + this.value + "}";
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getDestination() {
        return this.destination;
    }

    public void setDestination(Integer destination) {
        this.destination = destination;
    }
}
