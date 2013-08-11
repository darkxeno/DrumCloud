package com.codefixia.input.multitouch;

///////////////////////////////////////////////////////////////////////////////////
public class DragEvent extends TouchEvent {

	float x; // position
	float y;
	float dx; // movement 
	float dy; 
	int numberOfPoints;
	
	

	public float getX() {
		return x;
	}



	public float getY() {
		return y;
	}



	public float getDx() {
		return dx;
	}



	public float getDy() {
		return dy;
	}



	public int getNumberOfPoints() {
		return numberOfPoints;
	}



	DragEvent(float x, float y, float dx, float dy, int n) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		numberOfPoints = n;
	}
}