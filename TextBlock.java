import java.util.*;

public class TextBlock {
  TextLine line[];
  int current;
  
// -------------------------------------------------------------------  

  TextBlock (String[] text) {
    current=0;
    
    line=new TextLine[4];
    
    for (int i=0; i<4; i++)
       line[i]=new TextLine();
       
    for (int i=0; i<text.length; i++) {
      if (!line[current].fit(text[i]))
        current++;
        
      line[current].add(text[i]);
    }
  }
  
// -------------------------------------------------------------------  

  void flush () {
    for (int i=0; i<=current; i++)
      System.out.println(line[i].flush());
  }

// -------------------------------------------------------------------  

  int insert (int gameData[], int addr) {
    ArrayList<Integer> buffer;

    for (int i=0; i<=current; i++) {
      buffer=line[i].getBuffer();
      
      for (int j=0; j<buffer.size(); j++)
        gameData[addr++]=buffer.get(j);
      
      if (i==current) 
        gameData[addr++]=0;
      else {
        gameData[addr++]=0x81;
        gameData[addr++]=0xab;
      } 
    }
    
    return addr;
  }
  
}

