package com.codefixia.multitouch;

///////////////////////////////////////////////////////////////////////////////////
public class DragEvent extends TouchEvent {

	float x; // position
	float y;
	float dx; // movement 
	float dy; 
	int numberOfPoints;

	DragEvent(float x, float y, float dx, float dy, int n) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		numberOfPoints = n;
	}
}