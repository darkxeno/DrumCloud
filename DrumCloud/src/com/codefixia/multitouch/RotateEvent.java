package com.codefixia.multitouch;

///////////////////////////////////////////////////////////////////////////////////
public class RotateEvent extends TouchEvent {  

	float centerX;
	float centerY;
	float angle; // delta, in radians
	int numberOfPoints;

	RotateEvent(float centerX, float centerY, float angle, int n) {
		this.centerX = centerX;
		this.centerY = centerY;  
		this.angle = angle;
	}
}