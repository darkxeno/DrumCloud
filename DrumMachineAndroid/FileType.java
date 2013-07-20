package com.codefixia.drumcloud;

public enum FileType {
    File(SelectConstants.COLOR_FILE), 
    Folder(SelectConstants.COLOR_FOLDER),
    Up(SelectConstants.COLOR_UP);
    
    private final int color;

    FileType(int color) {
      this.color = color;
    }
    
    public int getColor() {
      return color;
    }
    
  }
