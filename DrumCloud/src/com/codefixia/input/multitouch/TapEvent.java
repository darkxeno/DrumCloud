package com.codefixia.input.multitouch;

///////////////////////////////////////////////////////////////////////////////////
public class TapEvent extends TouchEvent {

	public static final int SINGLE = 0;
	public static final int DOUBLE = 1;

	private float x;
	private float y;
	int type;

	TapEvent(float x, float y, int type) {
		this.setX(x);
		this.setY(y);
		this.type = type;
	}  

	boolean isSingleTap() {
		return (type == SINGLE) ? true : false;
	}

	boolean isDoubleTap() {
		return (type == DOUBLE) ? true : false;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
}