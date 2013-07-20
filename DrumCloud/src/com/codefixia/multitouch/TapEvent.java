package com.codefixia.multitouch;

///////////////////////////////////////////////////////////////////////////////////
public class TapEvent extends TouchEvent {

	public static final int SINGLE = 0;
	public static final int DOUBLE = 1;

	float x;
	float y;
	int type;

	TapEvent(float x, float y, int type) {
		this.x = x;
		this.y = y;
		this.type = type;
	}  

	boolean isSingleTap() {
		return (type == SINGLE) ? true : false;
	}

	boolean isDoubleTap() {
		return (type == DOUBLE) ? true : false;
	}
}