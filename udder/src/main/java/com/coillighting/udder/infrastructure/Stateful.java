package com.coillighting.udder.infrastructure;

public interface Stateful {

    /**
     * Return the Class whose instances communicate the public parameters
     * constituting this object's visible state.
     */
    public Class getStateClass();

    /**
     * Return an instance of the state Class specifying the values of the
     * public parameters in this object's visible state.
     */
    public Object getState();

    /**
     * Where Object state is an instance of the state Class, set the values of
     * the public parameters in this object's visible state, and adjust any
     * private state variables for consistency with the specified values.
     */
    public void setState(Object state) throws ClassCastException;

}
