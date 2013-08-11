package com.codefixia.ui;

import com.codefixia.drumcloud.DrumCloud;

public class ToggleButtonsBar {

	String[] buttonTexts;
	ToggleButton[] toggleButtons;
	boolean showMenuButton=false;
	float x,y,w,h;
	MenuButton menuButton;
	
	public ToggleButtonsBar(float x, float y, float w, float h) {
		this.x=x;
		this.y=y;
		this.w=w;
		this.h=h;		
	}	
	
	public boolean isShowMenuButton() {
		return showMenuButton;
	}

	public void setShowMenuButton(boolean showMenuButton) {
		this.showMenuButton = showMenuButton;
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
			toggleButtons[i].setActiveColor(DrumCloud.X.color(100));
			toggleButtons[i].setActiveColor(DrumCloud.X.greenColor);
			toggleButtons[i].setText(buttonTexts[i]);
			if(i==0)
				toggleButtons[i].setON(true);
		}
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
		menuButton.stopClick();
		for (int i = 0; i < buttonTexts.length; i++) {
			toggleButtons[i].cancelClick();
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
