package com.codefixia.input.multitouch;

import android.view.MotionEvent;

public class MultitouchHandler {

	//-------------------------------------------------------------------------------------
	// MULTI TOUCH EVENTS!

	void onTap( TapEvent event ) {
	  /*if ( event.isSingleTap() ) {
	    ellipse(event.x, event.y, 200, 200);
	    TAP = true;
	  }  
	  if ( event.isDoubleTap() ) {
	    rectMode(CENTER);
	    rect(event.x, event.y, 200, 200);
	    DOUBLE_TAP = true;
	  }*/
	}

	//- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void onFlick( FlickEvent event ) {
	  //println("FLICK! " + event.velocity.mag() );
	  //FLICK = true;
	}

	//- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void onDrag( DragEvent event ) { 
	  /*if (event.numberOfPoints == 1) {
	    DRAG = true;    
	    bx += event.dx;
	    by += event.dy;
	  }
	  else MULTI_DRAG = true;*/  
	}

	//- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void onRotate( RotateEvent event ) {
	  //ROTATE = true;
	  //r += event.angle;
	}

	//- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void onPinch( PinchEvent event ) {
	  //PINCH = true;  
	  //z += event.amount * 0.003;
	}

	//-------------------------------------------------------------------------------------
	// This is the stock Android touch event 
	boolean surfaceTouchEvent(MotionEvent event) {
		return false;
	  
	  // extract the action code & the pointer ID
	 /* int action = event.getAction();
	  int code   = action & MotionEvent.ACTION_MASK;
	  int index  = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;

	  float x = event.getX(index);
	  float y = event.getY(index);
	  int id  = event.getPointerId(index);
	  float pressure = event.getPressure(index);
	  println("Pressure:"+pressure);

	  // pass the events to the TouchProcessor
	  if ( code == MotionEvent.ACTION_DOWN || code == MotionEvent.ACTION_POINTER_DOWN) {
	    touch.pointDown(x, y, id, pressure);
	  }
	  else if (code == MotionEvent.ACTION_UP || code == MotionEvent.ACTION_POINTER_UP) {
	    touch.pointUp(event.getPointerId(index));
	  }
	  else if ( code == MotionEvent.ACTION_MOVE) {
	    int numPointers = event.getPointerCount();
	    for (int i=0; i < numPointers; i++) {
	      id = event.getPointerId(i);
	      x = event.getX(i);
	      y = event.getY(i);
	      touch.pointMoved(x, y, id);
	    }
	  } 
	  
	  return super.surfaceTouchEvent(event);*/
	}
	
	
}
