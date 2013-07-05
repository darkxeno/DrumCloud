public class Clickable {
  boolean clicked = false; // Is the object being clicked?
  boolean overed = false;
 
  float x,y,w,h;          // Location and size
  float offsetX, offsetY; // Mouseclick offset
  color fillColor=color(127),strokeColor=color(100);

  public Clickable(float tempX, float tempY, float tempW, float tempH) {
    x = tempX;
    y = tempY;
    w = tempW;
    h = tempH;
    offsetX = 0;
    offsetY = 0;
  }

  void drawState(){
    if(clicked){
      drawClicked();
    }else if(overed){
      drawOvered();
    }else{
      drawNormal();
    }
  }
  
  void draw(){
    this.drawNormal();
  }
  
  void draw(color fillColor){
    this.fillColor=fillColor;
    this.drawNormal();
  }
  
  void draw(color fillColor,color strokeColor){
    this.fillColor=fillColor;
    this.strokeColor=strokeColor;
    this.drawNormal();
  }
  
  void drawNormal(){
    stroke(strokeColor);
    fill(red(fillColor),green(fillColor),blue(fillColor),200);  
    rect(x,y,w,h);
    drawCommonContent();
  }

  void drawCommonContent(){
  }   
  
  void drawOvered(){
    stroke(200);
    fill(red(fillColor)*0.7,green(fillColor)*0.7,blue(fillColor)*0.7,alpha(fillColor));
    rect(x,y,w,h);
    drawCommonContent();    
  }
  
  void drawClicked(){
    //stroke(strokeColor.red()*0.8,strokeColor.green()*0.8,strokeColor.blue()*0.8,strokeColor.alpha()*0.8);
    fill(red(fillColor)*0.5,green(fillColor)*0.5,blue(fillColor)*0.5,alpha(fillColor));
    rect(x,y,w,h);
    drawCommonContent();    
  }


  // Is a point inside the rectangle (for click)?
  boolean isClicked(int mx, int my) {
    if (isOver(mx,my)) {
      clicked = true;
      // If so, keep track of relative location of click to corner of rectangle
      offsetX = x-mx;
      offsetY = y-my;
    }else{
      clicked =false;
    }
    
    return clicked;
  }
  
  boolean isOver(int mx, int my) {
    if (mx > x && mx < x + w && my > y && my < y + h) {
      overed=true;
    }else{
      overed=false;
    }
    return overed;
  }

  void stopClick() {
    clicked=false;
  }  
 

}
