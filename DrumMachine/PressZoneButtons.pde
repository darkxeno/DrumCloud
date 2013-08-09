public class PressZones extends Clickable {

  final short UP=1;
  final short DOWN=2;
  final short LEFT=3;
  final short RIGHT=3;

  private float showTime;
  private float hideTime;

  float animationTime=0.25;

  private String subZoneTexts[];
  private boolean showOtherZones=true; 
  private boolean dragReEnterEnabled=true;  

  String text="";

  public PressZones(float tempX, float tempY, float tempW, float tempH) {
    super(tempX, tempY, tempW, tempH);
    offsetY=0;
  }
  
  public float normalizedValue(){
    if(clicked){
     return map(offsetY,0,h,1,0);
    }
    else return 0.0;
  }

  void draw() {
    println("YES");
    if(showOtherZones)
      drawSubZones();
    fill(red(fillColor), green(fillColor), blue(fillColor), 100);   
    rect(x, y, w, h);
  }

  void drawCommonContent() {
    //println("x:"+x+" y:"+y+" w:"+w+" h:"+h);
    textSize(FontAdjuster.getSize(20));
    textAlign(CENTER);
    fill(200);
    text(text, x+(w*0.5), y+(h*0.7));
    if(showOtherZones)
      drawSubZones();    
  }
  
  void drawClicked(){
    drawNormal();
    ellipseMode(CENTER);
    stroke(200);
    noFill();
    ellipse(mouseX, mouseY, width*0.1, width*0.1);    
  }
  
  void drawOvered(){
    drawNormal();    
  }  

  void setSubZoneTexts(String[] texts) {
    if (texts.length>0) {
      subZoneTexts=texts;

    }
  }
  
  void setSubZoneColors(color[] colors) {
    if (subZoneTexts.length>0) {
      for (int i=0;i<subZoneTexts.length;i++) {
        
      }
    }
  }  

  void drawSubZones(){
      for (int i=0;i<subZoneTexts.length;i++) {
        //subZoneTexts[i].drawState();
      }
  }
  
  boolean isMoveOut(int mx, int my) {
    if(clicked){
      //println("isOver(mx, my):"+isOver(mx, my));
      if (!isOver(mx, my)){
        clicked=false;
        println("Moved out of zone");
        return true;
      }else{
        offsetX = mx-x;
        offsetY = my-y;        
      }
    }else{
      if(dragReEnterEnabled && isOver(mx, my)){
        clicked=true;
      }
    }
    
    return false;
  }

  boolean isClicked(int mx, int my) {
    if (isOver(mx, my)) {
      clicked = true;
      offsetX = mx-y;
      offsetY = my-y;
      showTime=millis();
      showOtherButtons();
    }
    else {
      clicked =false;
    }

    return clicked;
  } 
  
  boolean isClickStopped(){
    if(clicked){
      clicked=false;
      return true;
    }else{
      clicked=false;
      return false;    
    }
  }

  void showOtherButtons() {
    if (subZoneTexts!=null && subZoneTexts.length>0) {
      showOtherZones=true;
      for (int i=0;i<subZoneTexts.length;i++) {
      }
    }
  }

  void hideOtherButtons() {
    showOtherZones=false;
  }

  boolean isSelected(int mx, int my) {
    if (mx > x && mx < x + w && my > y && my < y + h) {
      overed=true;
    }else{
      for (int i=0;i<subZoneTexts.length;i++) {
          //subZoneTexts[i].isOver(mx,my);
      }      
      overed=false;
    }
    return overed;
  }  

  int buttonSelectedAt(int mx, int my) {
    int index=-1;
    if (clicked) {
      println("Released expandable button at mx:"+mx+" my:"+my);
      for (int i=0;i<subZoneTexts.length;i++) {
        /*if(subZoneTexts[i].isClicked(mx,my)){
          subZoneTexts[i].stopClick();
          println("Clicked expanded button at:"+i);
          index=i;
        }*/
      }
    }
    clicked = false;
    return index;
  }
}

