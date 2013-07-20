package com.codefixia.multitouch;

import processing.core.PVector;

///////////////////////////////////////////////////////////////////////////////////
public class FlickEvent extends TouchEvent { 

	float x;
	float y;
	PVector velocity;

	FlickEvent(float x, float y, PVector velocity) {
		this.x = x; 
		this.y = y;
		this.velocity = velocity;
	}
}