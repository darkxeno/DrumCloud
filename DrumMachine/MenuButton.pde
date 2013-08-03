public class MenuButton extends Clickable{
  
  String text="...";
  String activatedText;
  
  boolean ON=false;
  boolean blinkWhenOn=false;
  float blinkIntervalMS=500;
  boolean blinkOn=false;
  boolean released=false;
  
  public MenuButton(float tempX, float tempY, float tempW, float tempH) {
    super(tempX,tempY,tempW,tempH);
  }
  
  public void drawState(){
    if(overed){
      drawOvered();
    }else{
      drawNormal();
    }
  }
  
  public void drawOvered(){
    stroke(strokeColor);
    fill(red(fillColor),green(fillColor),blue(fillColor),200);  
    triangle(x, y, x+w, y, x+w, y+h);
    drawCommonContent();  
  }
  
  public void drawNormal(){
    stroke(strokeColor);
    fill(red(fillColor)-100,green(fillColor)-100,blue(fillColor)-100,200);  
    triangle(x, y, x+w, y, x+w, y+h);
    drawCommonContent();
  }  
  
  public void drawCommonContent() {
    textSize(FontAdjuster.getSize(18));
    textAlign(CENTER);
    pushMatrix();
    //translate(0,0);
    translate(x+(w*0.7f), y+(h*0.3f));
    rotate(radians(45));
    if(ON){
      fill(255);
      if(activatedText!=null)
        text(activatedText,0,0);
      else
        text(text,0,0);
    }
    else{
      fill(200);
      text(text,0,0);
    }    
    popMatrix();
  }
  
  public boolean isClicked(int mx, int my) {
    if (isOver(mx, my)) {
      clicked = true;
      offsetX = x-mx;
      offsetY = y-my;
      ON=!ON;
    }
    else {
      clicked =false;
    }

    return clicked;
  }
  
  boolean isReleased(int mx, int my) {
    if (isOver(mx, my)) {
      released = true;
      offsetX = x-mx;
      offsetY = y-my;
      ON=!ON;
    }
    else {
      released =false;
    }
    overed=false;    
    clicked =false;

    return released;
  }
    
    
  boolean cancelClick(int mx, int my) {
      clicked =false;
      overed=false;
      return true;
  }   

}
