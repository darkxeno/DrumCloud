package com.codefixia.ui;

import com.codefixia.drumcloud.DrumCloud;
import com.codefixia.utils.FontAdjuster;

public class ToggleButtonsBar {

	String[] buttonTexts;
	ToggleButton[] toggleButtons;
	boolean showMenuButton=false;
	boolean hasLabel=false;
	float x,y,w,h;
	MenuButton menuButton;
	private boolean momentary=false;
	private int activeColor=DrumCloud.X.greenColor;
	private boolean enabled=true;
	
	public ToggleButtonsBar(float x, float y, float w, float h) {
		this.x=x;
		this.y=y;
		this.w=w;
		this.h=h;		
	}
	
	public void setMomentary(boolean momentary) {
		this.momentary = momentary;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		for (int i = 0; i < toggleButtons.length; i++) {
			toggleButtons[i].setEnabled(enabled);
		}
	}

	public int getActiveColor() {
		return activeColor;
	}

	public void setActiveColor(int activeColor) {
		this.activeColor = activeColor;
	}

	public boolean isShowMenuButton() {
		return showMenuButton;
	}

	public void setShowMenuButton(boolean showMenuButton) {
		this.showMenuButton = showMenuButton;
	}

	public boolean hasLabel() {
		return hasLabel;
	}

	public void setHasLabel(boolean hasLabel) {
		this.hasLabel = hasLabel;
	}

	public void setButtons(String[] buttonTexts){
		this.buttonTexts=buttonTexts;
		toggleButtons=new ToggleButton[buttonTexts.length];
		float buttonWidth;
		if(showMenuButton){
			buttonWidth=w*0.85f/(float)buttonTexts.length;
			menuButton=new MenuButton(x+w*0.85f, y, w*0.15f, h);
			menuButton.setTriangleShape(false);
		}
		else
			buttonWidth=w/(float)buttonTexts.length;
		for (int i = 0; i < buttonTexts.length; i++) {
			toggleButtons[i]=new ToggleButton(x+(buttonWidth*i),y,buttonWidth,h);
			toggleButtons[i].setFillColor(DrumCloud.X.color(50));
			toggleButtons[i].setActiveColor(activeColor);
			toggleButtons[i].setText(buttonTexts[i]);
			if(!momentary && (i==0 && !hasLabel)||(i==1 && hasLabel))
				toggleButtons[i].setON(true);
			if(hasLabel && i==0){
				toggleButtons[i].setEnabled(false);
			}			
		}
	}
	
	public void drawLabel(String text){
			
	}
	
	public void draw(){
		for (int i = 0; i < buttonTexts.length; i++) {
			toggleButtons[i].drawState();
		}
		if(showMenuButton && menuButton!=null){
			menuButton.drawState();
		}
	}

	public boolean isOver(int mouseX, int mouseY) {
		boolean isOvered=false;
		/*for (int i = 0; i < buttonTexts.length; i++) {
			isOvered|=toggleButtons[i].isOver(mouseX, mouseY);
		}*/
		return isOvered;
	}

	public void stopClick() {
		if(showMenuButton)
			menuButton.stopClick();
		for (int i = 0; i < buttonTexts.length; i++) {
			toggleButtons[i].cancelClick();
			if(momentary){
				toggleButtons[i].setON(false);
			}
		}		
	}

	public int isButtonClicked(int mouseX, int mouseY) {
		int isClicked=-1;
		for (int i = 0; i < buttonTexts.length; i++) {
			if(!toggleButtons[i].isON() && toggleButtons[i].isClicked(mouseX, mouseY)){
				isClicked=i;
			}
		}
		if(isClicked!=-1){
			for (int i = 0; i < buttonTexts.length; i++) {
				if(i!=isClicked){
					toggleButtons[i].setON(false);
				}
			}						
		}
		return isClicked;
	}

	public boolean isMenuClicked(int mouseX, int mouseY) {
		if(showMenuButton)
			return menuButton.isClicked(mouseX, mouseY);
		else
			return false;
	}	
}
