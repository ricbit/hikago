import java.util.*;

// Contains an array of strings.
public class TextLine {
  ArrayList<String> line;
  // len is unused in this file.
  // space is 0 for first string, 1 for subsequent strings.
  // MAX is constant 28.
  int len,space,MAX; 
  
// -------------------------------------------------------------------  

  TextLine () {
    line=new ArrayList<String>();
    len=0;
    space=0;
    MAX=28;
  }
  
// -------------------------------------------------------------------  

// Returns length of given string. Special case: "@NAME" has length 8.
  int getLength(String s) {
  // Count occurrences of {0x87, 0x56, 0x87, 0x40, 0x87, 0x54}
  int numNames = 0;
  int last = s.lastIndexOf(ControlString.name);
  int current = s.indexOf(ControlString.name);
  while (current >= 0 && current <= last)
  {
    numNames++;
    current = s.indexOf(ControlString.name, current + 1);
  }
  
  // control bytes of @NAME has 6 bytes, but it should have length 8
    return s.length() + numNames * 2; 
  }

// -------------------------------------------------------------------  

// Returns whether adding s to the array will make the total length exceed MAX.
// s is not actually added.
  boolean fit (String s) {
    return (len()+space+getLength(s)<=MAX);
  }
  
// -------------------------------------------------------------------  

// Returns the sum of getLength() of all stored strings.
  int len() {
    int l=0;
    
    for (String s:line)
      l+=getLength(s);
      
    return l;  
  }
  
// -------------------------------------------------------------------  

// Adds s to the stored strings. If s is not the first string, " " will be added before it.
  void add (String s) {
    if (space>0) 
      line.add(" ");
    line.add(s);
  space = 1;
  }
  
// -------------------------------------------------------------------  

// Concatenates all strings.
  StringBuffer flush () {
    StringBuffer out=new StringBuffer();    
    
    for (String s:line)
      out.append(s);      
      
    return out;  
  }

// -------------------------------------------------------------------  

// Converts all characters in all strings to 8-bit integers, and returns them as an array.
// "@NAME"s are converted to a special 6-byte sequence.
  ArrayList<Integer> getBuffer() {
    ArrayList<Integer> buffer=new ArrayList<Integer>();

    for (String s:line) {
      for (int i=0; i<s.length(); i++) buffer.add(s.charAt(i)&0xFF);
    }
    
    return buffer;
  }

// -------------------------------------------------------------------  
  
}
