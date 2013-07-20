package com.codefixia.multitouch;

///////////////////////////////////////////////////////////////////////////////////
public class PinchEvent extends TouchEvent {

	float centerX;
	float centerY;
	float amount; // in pixels
	int numberOfPoints;

	PinchEvent(float centerX, float centerY, float amount, int n) {
		this.centerX = centerX;
		this.centerY = centerY;  
		this.amount = amount;
	}
}