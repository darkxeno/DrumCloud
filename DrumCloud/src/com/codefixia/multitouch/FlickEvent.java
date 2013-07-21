package com.codefixia.multitouch;

import processing.core.PVector;

///////////////////////////////////////////////////////////////////////////////////
public class FlickEvent extends TouchEvent { 

	float x;
	float y;
	PVector velocity;
	
	

	public float getX() {
		return x;
	}



	public float getY() {
		return y;
	}



	public PVector getVelocity() {
		return velocity;
	}



	FlickEvent(float x, float y, PVector velocity) {
		this.x = x; 
		this.y = y;
		this.velocity = velocity;
	}
}