package com.coillighting.udder.infrastructure;

/** A deserialized command to the udder server. Normally this command originates
 *  from an external client, often a browser. Commands do not know or care how
 *  they were wrapped for transport. They represent only their payload.
 *
 *  The command's payload is an arbitrary object (or null).
 */
public class Command {

    private Object value = null;
    private String path = null;

    public Command(String path, Object value) {
        this.value = value;
        this.path = path;
    }

    public String toString() {
        String v = null;
        if(value!=null) {
            v = value.getClass().getSimpleName();
        }
        return "Command(" + path + ", " + v + ")";
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
