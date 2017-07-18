import java.util.*;

// Contains an array of TextLines.
public class TextBlock {
  TextLine line[];
  // current stores the index of the last non-empty TextLine.
  int current;
  
// -------------------------------------------------------------------  

// Splits the given array of strings into 4 TextLines, making sure that
// no TextLine exceeds 28 characters.
// If given strings exceeds 4 TextLines, this will throw an exception.
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
  
  // New: constructs TextBlock from one string. The string will be
  // converted to byte array and have its control strings ("@NAME" and such)
  // replaced by corresponding control bytes.
  TextBlock (String text) {
  }
  
// -------------------------------------------------------------------  

// Prints each TextLine to the console.
  void flush () {
    for (int i=0; i<=current; i++)
      System.out.println(line[i].flush());
  }

// -------------------------------------------------------------------  

// Size of TextBlock in bytes, including the \0 at end.
  int size()
  {
    int size = 0;
    for (int i = 0; i <= current; i++)
    {
      size += line[i].getBuffer().size();
      size += 2;
    }
    size -= 1;
    
    return size;
  }

// Converts all TextLines to byte arrays, and overwrite gameData with these
// bytes starting from addr.
// At the end of each TextLine, an additional 0x81ab (on not last line) or 
// 0x0 (on last line) is written.
// Returns the address after last written byte.
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

