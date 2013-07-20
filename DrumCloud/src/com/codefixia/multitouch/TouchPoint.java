package com.codefixia.multitouch;

import processing.core.*;


public class TouchPoint {
	  
	  float x;
	  float y;
	  float px;
	  float py;
	  int id;
	  float pressure;
	  
	  // used for gesture detection
	  float angle;
	  float oldAngle;  
	  float pinch;
	  float oldPinch;

	  //-------------------------------------------------------------------------------------
	  /*TouchPoint(float x, float y, int id) {
	    this.x = x;
	    this.y = y;
	    this.px = x;
	    this.py = y;
	    this.id = id;
	    this.pressure=1.0;  
	  }*/
	  
	  TouchPoint(float x, float y, int id, float pressure) {
	    this.x = x;
	    this.y = y;
	    this.px = x;
	    this.py = y;
	    this.id = id;
	    this.pressure = pressure;  
	  }  

	  //-------------------------------------------------------------------------------------
	  void update(float x, float y) {
	    px = this.x;
	    py = this.y;
	    this.x = x;
	    this.y = y;
	  }

	  //-------------------------------------------------------------------------------------
	  void initGestureData(float cx, float cy) {  
	    pinch = oldPinch = PApplet.dist(x, y, cx, cy);
	    angle = oldAngle = PApplet.atan2( (y-cy), (x-cx) );
	  }
	 
	  //-------------------------------------------------------------------------------------
	  // delta x -- int to get rid of some noise
	  int dx() {
	    return (int)(x - px);
	  }
	  
	  //-------------------------------------------------------------------------------------
	  // delta y -- int to get rid of some noise
	  int dy() {
	    return (int)(y - py);
	  } 
	  
	  //-------------------------------------------------------------------------------------
	  void setAngle(float angle) {
	    oldAngle = this.angle;  
	    this.angle = angle;
	  }
	  
	  //-------------------------------------------------------------------------------------
	  void setPinch(float pinch) {
	     oldPinch = this.pinch;
	     this.pinch = pinch; 
	  }

	}

