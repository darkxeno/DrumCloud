public class ExpandableButtons extends Clickable {

  final short UP=1;
  final short DOWN=2;
  final short LEFT=3;
  final short RIGHT=3;

  private float showTime;
  private float hideTime;

  float animationTime=0.25;

  private String otherButtonsTexts[];
  private ExpandedButton otherButtonsPads[];
  private boolean showOtherButtons=false;

  short direction=DOWN;  

  String text="";

  public ExpandableButtons(float tempX, float tempY, float tempW, float tempH) {
    super(tempX, tempY, tempW, tempH);
  }

  void draw() {
    println("YES");
    if(showOtherButtons)
      drawOtherButtons();
    fill(red(fillColor), green(fillColor), blue(fillColor), 100);   
    rect(x, y, w, h);
  }

  void drawCommonContent() {
    //println("x:"+x+" y:"+y+" w:"+w+" h:"+h);
    textSize(FontAdjuster.getSize(20));
    textAlign(CENTER);
    fill(200);
    text(text, x+(w*0.5), y+(h*0.7));
    if(showOtherButtons)
      drawOtherButtons();    
  }

  void setOtherButtons(String[] texts) {
    if (texts.length>0) {
      otherButtonsTexts=texts;
      otherButtonsPads=new ExpandedButton[texts.length];
      for (int i=0;i<otherButtonsTexts.length;i++) {
        if (direction==UP || direction==DOWN) {
          otherButtonsPads[i]=new ExpandedButton(x, y+(i+1)*((direction==DOWN?h:-h)+strokeWeight), w, h);
        }else{
          otherButtonsPads[i]=new ExpandedButton(x+(i+1)*((direction==RIGHT?w:-w)+strokeWeight), y, w, h);
        }
        otherButtonsPads[i].fillColor=fillColor;
        otherButtonsPads[i].text=texts[i];
      }
    }
  }
  
  void setOtherButtonColors(color[] colors) {
    if (otherButtonsPads.length>0) {
      for (int i=0;i<otherButtonsPads.length;i++) {
        if(otherButtonsPads[i]!=null && colors.length>i){
          otherButtonsPads[i].fillColor=colors[i]; 
        }else{
          otherButtonsPads[i].fillColor=fillColor;
        }
      }
    }
  }  

  void drawOtherButtons(){
      for (int i=0;i<otherButtonsTexts.length;i++) {
        otherButtonsPads[i].drawState();
      }
  }

  boolean isClicked(int mx, int my) {
    if (isOver(mx, my)) {
      clicked = true;
      offsetX = x-mx;
      offsetY = y-my;
      showTime=millis();
      showOtherButtons();
    }
    else {
      clicked =false;
    }

    return clicked;
  } 

  void showOtherButtons() {
    if (otherButtonsTexts!=null && otherButtonsTexts.length>0) {
      showOtherButtons=true;
      for (int i=0;i<otherButtonsTexts.length;i++) {
      }
    }
  }

  void hideOtherButtons() {
    showOtherButtons=false;
  }

  boolean isSelected(int mx, int my) {
    if (mx > x && mx < x + w && my > y && my < y + h) {
      overed=true;
    }else{
      for (int i=0;i<otherButtonsPads.length;i++) {
          otherButtonsPads[i].isOver(mx,my);
      }      
      overed=false;
    }
    return overed;
  }  

  int buttonSelectedAt(int mx, int my) {
    int index=-1;
    if (clicked) {
      println("Released expandable button at mx:"+mx+" my:"+my);
      for (int i=0;i<otherButtonsPads.length;i++) {
        if(otherButtonsPads[i].isClicked(mx,my)){
          otherButtonsPads[i].stopClick();
          println("Clicked expanded button at:"+i);
          index=i;
        }
      }
    }
    hideOtherButtons();
    clicked = false;
    return index;
  }
}

