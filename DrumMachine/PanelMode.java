public enum PanelMode 
{
FILTER(0),ECHO(1),PITCH(2),SPECTRO(3);

  private int value;

  PanelMode(int value){
    this.value=value;
  }

  public int value(){
    return value;
  }
  
  public static PanelMode fromInt(int value){
     switch(value){
       case 0:
         return FILTER;
       case 1:
         return ECHO;
       case 2:
         return PITCH;
       case 3:
         return SPECTRO;
       default:
         return SPECTRO;
     }
  }
};
