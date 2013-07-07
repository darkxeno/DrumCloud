// Click and Drag an object
// Daniel Shiffman 

// A class for a draggable thing

class Draggable {
  boolean dragging = false; // Is the object being dragged?
  boolean rollover = false; // Is the mouse over the ellipse?
  
  float x,y,w,h;          // Location and size
  float offsetX, offsetY; // Mouseclick offset
  float minX,minY,maxX,maxY;
  boolean limitedX=false,limitedY=false;
  float expandedFactor=1.0;
  boolean slideOnClick=true;
  float minXZone,minYZone,maxXZone,maxYZone;

  Draggable(float tempX, float tempY, float tempW, float tempH) {
    x = tempX;
    y = tempY;
    w = tempW;
    h = tempH;
    offsetX = 0;
    offsetY = 0;
    calculateClickZone();
  }
  
  void limitY(float min,float max){
    limitedY=true;
    minY=min;
    maxY=max;
    calculateClickZone();
  }
  
  void limitX(float min,float max){
    limitedX=true;
    minX=min;
    maxX=max;
    calculateClickZone();    
  }  

  void calculateClickZone(){
    float xExp=(expandedFactor-1.0)*w;
    float yExp=(expandedFactor-1.0)*h;
    
    if(limitedX){
      minXZone=minX-(xExp*0.5);
      maxXZone=maxX+(xExp*0.5)+w;
    }else{
      minXZone=x-(xExp*0.5);
      maxXZone=x+w+(xExp*0.5);    
    }
    if(limitedY){
      minYZone=minY-(yExp*0.5);
      maxYZone=h+maxY+(yExp*0.5);    
    }else{
      minYZone=y-(yExp*0.5);
      maxYZone=h+y+(yExp*0.5);
    }  
  }

  // Method to display
  void debugDisplay() {
    stroke(255);
    //if(expandedFactor!=1.0){
      fill(255,0,0);
      rect(minXZone,minYZone,maxXZone-minXZone,maxYZone-minYZone);    
    //}
    
    if (dragging) fill (50);
    else if (rollover) fill(100);
    else fill(175,200);
    rect(x,y,w,h);
  }
  
  void draw() {
    strokeWeight(3);
    fill(0,0,0,0.5);
    rect(minXZone,minYZone,maxXZone-minXZone,maxYZone-minYZone);    
    stroke(200);
    if (dragging) fill (50);
    else if (rollover) fill(100);
    else fill(175,200);
    rect(x,y,w,h);
  }  

  // Is a point inside the rectangle (for click)?
  void clicked(int mx, int my) {
    calculateClickZone();
    //println("zone (X:"+minXZone+" to "+maxXZone+" Y:"+minYZone+" to "+maxYZone+",) mouse:("+mx+","+my+")");
    if (mx > minXZone && mx < maxXZone && my > minYZone && my < maxYZone) {
    //if (mx > x && mx < x + w && my > y && my < y + h) {
      dragging = true;
      // If so, keep track of relative location of click to corner of rectangle
      offsetX = x-mx;
      offsetY = y-my;
      if(slideOnClick){
        if(limitedY)y=my-h*0.5;
        if(limitedX)x=mx-w*0.5;
        intoLimits();
        println("moved to x:"+x+" y:"+y);
      }    
    }
  }
  
  void intoLimits(){
    if(limitedY){
      if(y<minY)
        y=minY;
      else if(y>maxY)
         y=maxY;   
    }
    if(limitedX){
      if(y<minX)
        x=minX;
      else if(y>maxX)
         x=maxX;   
    }    
  }
  
  // Is a point inside the rectangle (for rollover)
  void rollover(int mx, int my) {
    if (mx > minXZone && mx < maxXZone && my > minYZone && my < maxYZone) {
    //if (mx > x && mx < x + w && my > y && my < y + h) {
      rollover = true;
    } else {
      rollover = false;
    }
  }

  // Stop dragging
  void stopDragging() {
    dragging = false;
  }
  
  // Drag the rectangle
  void drag(int mx, int my) {
    if (dragging) {
      if(!limitedX || (mx + offsetX>=minX && mx + offsetX<=maxX))
        x = mx + offsetX;
      if(!limitedY || (my + offsetY>=minY && my + offsetY<=maxY))
        y = my + offsetY;
    }
  }
  
  void dragVertically(int my) {
    if (dragging) {
      if(!limitedY)
        y = my + offsetY;
      else if(my + offsetY<minY)
        y = minY;
      else if(my + offsetY>maxY)
        y = maxY;
      else y = my + offsetY;
    }
  }
  
  void dragHorizontally(int mx) {
    if (dragging) {
      if(!limitedX)
        x = mx + offsetX;
      else if(mx + offsetX<minX)
        x = minX;
      else if(mx + offsetX>maxX)
        x = maxX;
      else x = mx + offsetX;
    }
  }  

}
