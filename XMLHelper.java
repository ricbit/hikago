import java.util.*;
import org.xml.sax.*;  
import org.w3c.dom.*;

// A custom exception that accepts a string on construction.
class XMLError extends Exception {
  XMLError (String s) {
    super ("XMLError: "+s);
  }
}

public class XMLHelper {
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
}
