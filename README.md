# "Hikaru no Go" GBA Translation

[Homepage](http://www.ricbit.com/mundobizarro/hikaru.php)

## Usage

0. Install java runtime.
1. Prepare your own ROM of the game, name it "hikago.gba", and place it in the same directory as Script.java.
2. `javac *.java`
3. `java Script hikago.xml insert`

## Code structure

- `class TextBlock`: one `TextBlock` object corresponds to one `<text>` element in hikago.xml, and contains an array of `TextLine` objects.
- `class TextLine`: one `TextLine` object corresponds to one line of in-game text, and contains an array of java strings.
- `class Script`: the main class, it reads data from files, processes them, applies the translation to hikago.gba, and writes the result to _hikago.gba.
- `class ControlString`: one `ControlString` object corresponds to one special control string in hikago.xml, such as "@NAME@".