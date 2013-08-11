package com.codefixia.input.multitouch;

///////////////////////////////////////////////////////////////////////////////////
public class RotateEvent extends TouchEvent {  

	float centerX;
	float centerY;
	float angle; // delta, in radians
	int numberOfPoints;
	
	

	public float getCenterX() {
		return centerX;
	}



	public float getCenterY() {
		return centerY;
	}



	public float getAngle() {
		return angle;
	}



	public int getNumberOfPoints() {
		return numberOfPoints;
	}



	RotateEvent(float centerX, float centerY, float angle, int n) {
		this.centerX = centerX;
		this.centerY = centerY;  
		this.angle = angle;
	}
}