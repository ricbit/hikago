public class ControlString
{
  public String controlString;
  public String controlBytes;
  // This special control string is refered to in TextLine.java
  public static String name = new String(new char[]{0x87, 0x56, 0x87, 0x40, 0x87, 0x54});
  
  public ControlString(String string, char[] bytes)
  {
    controlString = string;
    controlBytes = new String(bytes);
  }
  
  public ControlString(String string, String bytes)
  {
    controlString = string;
    controlBytes = bytes;
  }
}
