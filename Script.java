/* Script parser for GBA translations */
/* by Ricardo Bittencourt */
/* started in 2006.3.19 */
/* last modification in 2006.3.28 */

import java.util.*;
import java.io.*;
import java.nio.charset.*;
import java.awt.image.*;
import javax.xml.parsers.*;
import javax.imageio.*;
import org.xml.sax.*;  
import org.w3c.dom.*;

// A custom exception that accepts a string on construction.
class XMLError extends Exception {
  XMLError (String s) {
    super ("XMLError: "+s);
  }
}

public class Script {
  
// -------------------------------------------------------------------  
// GLOBALS

  static int[] gameData;
  // Sets whether we are reading from or writing into ROM.
  static Boolean extract;
  // A cursor into gameData for writing text.
  static int freePos;
  static ArrayList<ControlString> controlStrings;

// -------------------------------------------------------------------  

// Finds the given attribute of given element.
  public static String getAttribute(Node n, String attribute) {
    return n.getAttributes().getNamedItem(attribute).getNodeValue();
  }

// -------------------------------------------------------------------  

// Returns the text within given element. Throws XMLError if no text exists.
  public static String getText(Node n) throws XMLError {
    NodeList list = n.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      if (list.item(i).getNodeType() == Document.TEXT_NODE)
        return list.item(i).getNodeValue();
    }
  
    throw new XMLError ("Node " + n.getNodeName() + " contains no text");
  }

// -------------------------------------------------------------------  

// Finds and returns a child element of n with given name. Throws XMLError if no such element exists.
  public static Node getChildElement(Node n, String element) throws XMLError {
    NodeList list = n.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) 
      if (list.item(i).getNodeType() == Document.ELEMENT_NODE &&
          list.item(i).getNodeName().equals(element)) {
        return list.item(i);
      }
      
    throw new XMLError ("No element named " + element + " in node " + n.getNodeName());
  }

// -------------------------------------------------------------------  

// Within node n, looks for a child element called element, and returns its text.
  public static String getTextInChildElement (Node n, String element) throws XMLError {
	return getText(getChildElement(n, element));
  }

// -------------------------------------------------------------------  

// Within node n, looks for a child element called element, and returns its queried attribute.
// If no such element exists, throws XMLError.
  public static String getAttributeInChildElement (Node n, String element, String attribute) throws XMLError {
	return getAttribute(getChildElement(n, element), attribute);
  }
  
// -------------------------------------------------------------------  

// Recursively find and return all elements named element inside n.
  public static ArrayList<Node> recursiveFindElements (Node n, String element) {
    ArrayList<Node> result = new ArrayList<Node>();
    NodeList list = n.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) {
      Node thisNode = list.item(i);
      if (thisNode.getNodeType() != Document.ELEMENT_NODE) continue;
      if (thisNode.getNodeName().equals(element)) {
        result.add(thisNode);
      } else {
        result.addAll(recursiveFindElements(thisNode, element));
      }
    }
  
    return result;
  }

// -------------------------------------------------------------------  

// Reads file as a byte array.
  public static int[] readFile (String name) throws XMLError {
    DataInput in = null;
    int size;    
    File f;
        
    // Read file from disk
    f = new File(name);
    if (!f.exists()) 
      throw new XMLError ("File <" + name + "> not found");
      
    size = (int)(f.length());    
    int[] buffer = new int[size];
    byte[] byteData = new byte[size];
    try {
      in = new DataInputStream (new FileInputStream (name));
      in.readFully(byteData, 0, size);  
      for (int i = 0; i < size; i++)
        buffer[i] = (int)byteData[i] & 0xFF;
    } catch (IOException fe) {
      fe.printStackTrace();
    }     
    
    return buffer;
  }
    
// -------------------------------------------------------------------  

// Writes buffer as a byte array into file.
  public static void writeFile (int[] buffer, String name) throws XMLError {
    DataOutputStream out = null;
        
    // Write file to disk
    byte[] byteData = new byte[gameData.length];
    for (int i = 0; i < gameData.length; i++)
      byteData[i] = (byte)gameData[i];
    try {
      out = new DataOutputStream (new FileOutputStream (name));
      out.write(byteData);  
      out.close();
    } catch (IOException fe) {
      fe.printStackTrace();
    }     
    
    return;
  }
    
// -------------------------------------------------------------------  

// Reads 48 bytes from address (index * 48) to construct an IndexColorModel with 16 colors.
  public static IndexColorModel readPalette (String name, int index) throws XMLError {
    int[] rawPalette;
    byte[] r, g, b;
    
    rawPalette = readFile(name);
    r = new byte[16];
    g = new byte[16];
    b = new byte[16];
    for (int i = 0; i < 16; i++) {
      r[i] = (byte) rawPalette[index*16*3 + i*3 + 0];
      g[i] = (byte) rawPalette[index*16*3 + i*3 + 1];
      b[i] = (byte) rawPalette[index*16*3 + i*3 + 2];
    } 
    IndexColorModel finalPalette = new IndexColorModel (8, 16, r, g, b);
    
    return finalPalette;
  }

// -------------------------------------------------------------------  

// Sets pixel (x, y) of image to value.
  public static void mySetPixel (BufferedImage image, int x, int y, int value) {
    int pixel[];
    WritableRaster wr = image.getRaster();
    
    pixel = new int [1];
    pixel[0] = value;
    wr.setPixel(x, y, pixel);
  }

// -------------------------------------------------------------------  

// Gets pixel (x, y) from image.
  public static int myGetPixel (BufferedImage image, int x, int y) {
    int pixel[];
    WritableRaster wr = image.getRaster();
    
    pixel = new int [1];
    pixel = wr.getPixel(x, y, pixel);
    return pixel[0];
  }

// -------------------------------------------------------------------  

// Reads an image's information from node, reads the image from disk, then
// writes the image bytes into gameData.
  public static void writeGraphic (Node n) throws XMLError {
    int addr, width, height, index;
    String name;
    IndexColorModel palette;
    BufferedImage image = null;
    
    addr = Integer.parseInt(getTextInChildElement(n, "addr"), 16);
    width = Integer.parseInt(getTextInChildElement(n, "width"));
    height = Integer.parseInt(getTextInChildElement(n, "height"));
    name = "_" + getTextInChildElement(n, "name") + ".bmp";
    
    File f = new File(name);
    if (!f.exists())
      return;
      
    try {
      image = ImageIO.read(f);
    } catch (IOException fe) {
      fe.printStackTrace();
    }     

    for (int i = 0; i < height*width*4*8; i++)
       gameData[addr+i] = 0;

    for (int hh = 0; hh < height; hh++)
      for (int ww = 0; ww < width; ww++)
        for (int j = 0; j < 8; j++) 
          for (int i = 0; i < 4; i++)
            for (int ii = 0; ii < 2; ii++) 
              gameData[addr + i + j*4 + ww*4*8 + hh*4*8*width] |=
                (myGetPixel(image, ww*8 + i*2 + ii, hh*8 + j) & 0xf) << (ii*4);

    System.out.println ("inserted graphic <" + name + ">.");
  }
 
// -------------------------------------------------------------------  

// Reads an image's information from node, reads the image from gameData, then
// writes the image to disk.
// There seems to be 32 bytes per pixel?
  public static void readGraphic (Node n) throws XMLError {
    int addr, width, height, index;
    String name;
    IndexColorModel palette;
    BufferedImage image;
    
    addr = Integer.parseInt(getTextInChildElement(n, "addr"), 16);
    width = Integer.parseInt(getTextInChildElement(n, "width"));
    height = Integer.parseInt(getTextInChildElement(n, "height"));
    name = getTextInChildElement(n, "name") + ".bmp";
    index = Integer.parseInt(getAttributeInChildElement(n, "palette", "index"));
    palette = readPalette(getTextInChildElement(n, "palette"), index);
    
    image = new BufferedImage (width*8, height*8, BufferedImage.TYPE_BYTE_BINARY, palette);

    for (int hh = 0; hh < height; hh++)
      for (int ww = 0; ww < width; ww++)
        for (int j = 0; j < 8; j++) 
          for (int i = 0; i < 4; i++)
            for (int ii = 0; ii < 2; ii++) 
              mySetPixel (image,
                ww*8 + i*2 + ii, // x
                hh*8 + j, // y
                0xf & (gameData[addr + (ww + hh*width)*4*8 + j*4 + i] >> (ii*4)) // value
              );
              
    //System.out.println (((IndexColorModel)(image.getColorModel())).getMapSize());
    
    try {
      ImageIO.write(image, "bmp", new File(name));
    } catch (IOException fe) {
      fe.printStackTrace();
    }     
    
    System.out.println ("extracted graphic <" + name + ">.");
  }

// -------------------------------------------------------------------  

// Reads <pointer> from node, reads 4 bytes from gameData[pointer] that
// forms addr, and reads bytes from gameData[addr] and writes to a .sjs file until 0 is met.
  public static void readText (Node n) throws XMLError {
    int pointer, addr;
    DataOutputStream out;
    
    String pointerStr = getAttribute(n, "pointer");
  
    pointer = Integer.parseInt(pointerStr, 16);
    addr = 0;
    for (int i = 0; i < 4; i++)
      addr |= gameData[pointer + i] << (i*8);
    addr -= 0x8000000;  

    try {
      out = new DataOutputStream (new FileOutputStream (pointerStr + ".sjs"));
      while (gameData[addr] != 0) {
        out.writeByte(gameData[addr++]);  
        out.writeByte(gameData[addr++]);  
      }
      out.close();
    } catch (IOException fe) {
      fe.printStackTrace();
    }     
    
    System.out.println ("extracted text <" + pointerStr + ">.");    
  }    

// -------------------------------------------------------------------  

// Reads <pointer> from node, writes 4 bytes to gameData[pointer] that
// forms addr (freePos + 0x80000000), and reads text from <data> and writes bytes to gameData[addr].
// freePos is updated after writing.
  public static void writeText (Node n) throws XMLError {
    int pointer, addr;
    TextBlock text;
       
    String pointerStr = getAttribute(n, "pointer");
    String translated = getText(n);
    if (translated.equals("")) return;
  
    pointer = Integer.parseInt(pointerStr, 16);
  
    // First, attempt to write translated text into position of original text.
    addr = 0;
    for (int i = 0; i < 4; i++) addr |= gameData[pointer + i] << (i * 8);
    addr -= 0x8000000;
    int readPtr = addr;
    while (gameData[readPtr] != 0) readPtr++;
    int originalLength = readPtr - addr + 1; // Including \0
  
    for (int i = 0; i < controlStrings.size(); i++) {
      translated = translated.replace(
        controlStrings.get(i).controlString, 
        controlStrings.get(i).controlBytes
      );
    }
  
    text = new TextBlock(translated.split("(?<=.) (?=.)")); // This allows a preceding space and a trailing space
    int newLength = text.size(); // Also including \0
  
    if (newLength <= originalLength) {
      // Translated text is shorter: able to write
      text.insert(gameData, addr);
    } else {
      // Translated text is longer: write to end of ROM
      System.out.println("===================================================");
      System.out.println("Warning: text " + pointerStr + " is written to end of ROM.");
      System.out.println("Original text is " + originalLength + " bytes, but translated text is " + newLength + " bytes.");
      System.out.println("Translated text: " + getText(n));
      addr = freePos + 0x8000000;
      for (int i = 0; i < 4; i++)
        gameData[pointer+i] = (addr >> (i*8)) & 0xFF;  
      freePos = text.insert(gameData, freePos);
    }
  }    

// -------------------------------------------------------------------  

// Processes all <graphic>s and <text>s in node. May read from or write into ROM, depending on
// value of extract.
  public static void readTranslation (Node n) throws XMLError {
    NodeList list;
    
    list = n.getChildNodes();
    
    // Search for the game rom
    gameData = readFile(getTextInChildElement(n, "game"));    
    System.out.println ("Found <" + getTextInChildElement(n, "game") + ">, " + gameData.length + " bytes.");
        
    // Process Graphics
    ArrayList<Node> allGraphics = recursiveFindElements(n, "graphic");
    for (int i = 0; i < allGraphics.size(); i++) {
      if (extract)
        readGraphic(allGraphics.get(i));
      else
        writeGraphic(allGraphics.get(i));
    }
  
  /*
    for (int i=0; i<list.getLength(); i++) 
      if (list.item(i).getNodeType()==Document.ELEMENT_NODE && list.item(i).getNodeName().equals("graphic")) {
        if (extract)
          readGraphic(list.item(i));
        else  
          writeGraphic(list.item(i));
      }
    */
  
    // Process Text
    ArrayList<Node> allStrings = recursiveFindElements(n, "text");
    for (int i = 0; i < allStrings.size(); i++) {
      if (extract)
        readText(allStrings.get(i));
      else
        writeText(allStrings.get(i));
    }
  
  /*
    for (int i=0; i<list.getLength(); i++) 
      if (list.item(i).getNodeType()==Document.ELEMENT_NODE && list.item(i).getNodeName().equals("text")) {
        if (extract)
          readText(list.item(i));
        else  
          writeText(list.item(i));
      }
    */
  
    // Write back
    if (!extract) {
      writeFile(gameData, "_" + getTextInChildElement(n, "game"));
      System.out.println ("Finished <_" + getTextInChildElement(n, "game") + ">.");
    }     
    
  }

// -------------------------------------------------------------------  

// Processes all <translation>s in node.
  public static void readDocument (Node n) throws XMLError {
    NodeList list;
    
    // Check for document-type
    if (n.getNodeType() != Document.DOCUMENT_NODE) 
      throw new XMLError("Document not found");
      
    list = n.getChildNodes();
    for (int i = 0; i < list.getLength(); i++) 
      if (list.item(i).getNodeType() == Document.ELEMENT_NODE &&
          list.item(i).getNodeName().equals("translation"))
        readTranslation(list.item(i));
  }

// -------------------------------------------------------------------  

  public static void main(String argv[]) {
    // Check for command line usage
    if (argv.length != 2) {
      System.err.println("Usage: java Script filename (extract/insert)");
      System.exit(1);
    }
  
    if (!argv[1].equals("extract") && !argv[1].equals("insert")) {
      System.err.println("Usage: java Script filename (extract/insert)");
      System.exit(1);
    }
    
    // Init globals
    gameData = null;
    extract = argv[1].equals("extract");
    freePos = 0x7fade0;
    int startFreePos = 0x7fade0;
    controlStrings = new ArrayList<ControlString>();
    controlStrings.add(new ControlString("@NAME@", ControlString.name));
    controlStrings.add(new ControlString("@NEWLINE@", new char[]{0x81, 0xab}));
    controlStrings.add(new ControlString("@DPAD@", new char[]{0x84, 0x42}));
    controlStrings.add(new ControlString("@ABUTTON@", new char[]{0x84, 0x43}));
    controlStrings.add(new ControlString("@BBUTTON@", new char[]{0x84, 0x44}));
    controlStrings.add(new ControlString("@LBUTTON@", new char[]{0x84, 0x47}));
    controlStrings.add(new ControlString("@RBUTTON@", new char[]{0x84, 0x46}));
    controlStrings.add(new ControlString("@SELECTBUTTON@", new char[]{0x84, 0x48, 0x84, 0x49}));
    controlStrings.add(new ControlString("@STARTBUTTON@", new char[]{0x84, 0x4a, 0x84, 0x4b}));
    controlStrings.add(new ControlString("@STRESSRED@", new char[]{0x87, 0x55}));
    controlStrings.add(new ControlString("@STRESS@", new char[]{0x87, 0x56}));
    controlStrings.add(new ControlString("@ENDSTRESS@", new char[]{0x87, 0x54}));
    
    // Create xml factory
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    
    // Parse the document and check for errors
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse( new File(argv[0]) );
      readDocument (document);
    
      int totalFreeBytes = 0x800000 - startFreePos;
      int usedFreeBytes = freePos - startFreePos;
      System.out.println(usedFreeBytes + " of " + totalFreeBytes + " end-of-ROM bytes used.");
 
    } catch (SAXException sxe) {
      // Error generated during parsing
      Exception  x = sxe;
      if (sxe.getException() != null)
        x = sxe.getException();
      x.printStackTrace();

    } catch (ParserConfigurationException pce) {
      // Parser with specified options can't be built
      pce.printStackTrace();

    } catch (IOException ioe) {
      // I/O error
      ioe.printStackTrace();

    } catch (XMLError xe) {
      // XML error
      xe.printStackTrace();
    }
  }
  
// -------------------------------------------------------------------  
}
