package com.coillighting.udder;


public interface Stateful {

	public Class getStateClass();

	public Object getState();

	public void setState(Object state) throws ClassCastException;

}
