package com.codefixia.multitouch;

///////////////////////////////////////////////////////////////////////////////////
public class PinchEvent extends TouchEvent {

	float centerX;
	float centerY;
	private float amount; // in pixels
	int numberOfPoints;

	PinchEvent(float centerX, float centerY, float amount, int n) {
		this.centerX = centerX;
		this.centerY = centerY;  
		this.amount=amount;
	}

	public float getAmount() {
		return amount;
	}

	public float getCenterX() {
		return centerX;
	}

	public float getCenterY() {
		return centerY;
	}

	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	
}