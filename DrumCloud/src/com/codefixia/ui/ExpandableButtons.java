package com.codefixia.ui;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.utils.FontAdjuster;


public class ExpandableButtons extends Clickable {

  final short UP=1;
  final short DOWN=2;
  final short LEFT=3;
  final short RIGHT=3;

  private float showTime;
  private float hideTime;

  float animationTime=0.25f;

  private String otherButtonsTexts[];
  private ExpandedButton otherButtonsPads[];
  private boolean showOtherButtons=false;

  short direction=DOWN;  

  private String text="";

  public ExpandableButtons(float tempX, float tempY, float tempW, float tempH) {
    super(tempX, tempY, tempW, tempH);
  }

  public void draw() {
    DrumCloud.println("YES");
    if(showOtherButtons)
      drawOtherButtons();
    DrumCloud.X.fill(DrumCloud.X.red(getFillColor()), DrumCloud.X.green(getFillColor()), DrumCloud.X.blue(getFillColor()), 100);   
    DrumCloud.X.rect(getX(), getY(), getW(), getH());
  }

  public void drawCommonContent() {
    //println("x:"+x+" y:"+y+" w:"+w+" h:"+h);
    DrumCloud.X.textSize(FontAdjuster.getSize(20));
    DrumCloud.X.textAlign(DrumCloud.CENTER);
    DrumCloud.X.fill(200);
    DrumCloud.X.text(getText(), getX()+(getW()*0.5f), getY()+(getH()*0.7f));
    if(showOtherButtons)
      drawOtherButtons();    
  }

  public void setOtherButtons(String[] texts) {
    if (texts.length>0) {
      otherButtonsTexts=texts;
      otherButtonsPads=new ExpandedButton[texts.length];
      for (int i=0;i<otherButtonsTexts.length;i++) {
        if (direction==UP || direction==DOWN) {
          otherButtonsPads[i]=new ExpandedButton(DrumCloud.X, getX(), getY()+(i+1)*((direction==DOWN?getH():-getH())+strokeWeight), getW(), getH());
        }else{
          otherButtonsPads[i]=new ExpandedButton(DrumCloud.X, getX()+(i+1)*((direction==RIGHT?getW():-getW())+strokeWeight), getY(), getW(), getH());
        }
        otherButtonsPads[i].setFillColor(fillColor);
        otherButtonsPads[i].text=texts[i];
      }
    }
  }
  
  public void setOtherButtonColors(int[] colors) {
    if (otherButtonsPads.length>0) {
      for (int i=0;i<otherButtonsPads.length;i++) {
        if(otherButtonsPads[i]!=null && colors.length>i){
          otherButtonsPads[i].setFillColor(colors[i]); 
        }else{
          otherButtonsPads[i].setFillColor(fillColor);
        }
      }
    }
  }  

  public void drawOtherButtons(){
      for (int i=0;i<otherButtonsTexts.length;i++) {
        otherButtonsPads[i].drawState();
      }
  }

  public boolean isClicked(int mx, int my) {
    if (isOver(mx, my)) {
      clicked = true;
      setOffsetX(getX()-mx);
      setOffsetY(getY()-my);
      showTime=DrumCloud.X.millis();
      showOtherButtons();
    }
    else {
      clicked =false;
    }

    return clicked;
  } 

  public void showOtherButtons() {
    if (otherButtonsTexts!=null && otherButtonsTexts.length>0) {
      showOtherButtons=true;
      for (int i=0;i<otherButtonsTexts.length;i++) {
      }
    }
  }

  public void hideOtherButtons() {
    showOtherButtons=false;
  }

  public boolean isSelected(int mx, int my) {
    if (mx > getX() && mx < getX() + getW() && my > getY() && my < getY() + getH()) {
      overed=true;
    }else{
      for (int i=0;i<otherButtonsPads.length;i++) {
          otherButtonsPads[i].isOver(mx,my);
      }      
      overed=false;
    }
    return overed;
  }  

  public int buttonSelectedAt(int mx, int my) {
    int index=-1;
    if (clicked) {
      DrumCloud.println("Released expandable button at mx:"+mx+" my:"+my);
      for (int i=0;i<otherButtonsPads.length;i++) {
        if(otherButtonsPads[i].isClicked(mx,my)){
          otherButtonsPads[i].stopClick();
          DrumCloud.println("Clicked expanded button at:"+i);
          index=i;
        }
      }
    }
    hideOtherButtons();
    clicked = false;
    return index;
  }

public String getText() {
	return text;
}

public void setText(String text) {
	this.text = text;
}
}