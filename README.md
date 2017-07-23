# "Hikaru no Go" GBA Translation

[Homepage](http://www.ricbit.com/mundobizarro/hikaru.php)

## Usage

0. Install JDK.
1. Prepare your own ROM of the game, name it "hikago.gba", and place it in the same directory as Script.java.
2. `javac *.java`
3. `java Script hikago.xml insert`

## Understanding hikago.xml

### Text

Each `<text>` element in hikago.xml corresponds to one translated string. They may be organized under parent elements of arbitrary names, but the script only recursively looks for `<text>` elements and ignores everything else.

Pointers exist in the ROM. The `pointer` attribute of a `<text>` element gives the address of a **pointer** to the in-game string. For example, for this element:

```
<text pointer="2d52a0">1.Go is?</text>
```

The script will go to 0x002d52a0 of the ROM:

```
002d52a0  1C 54 2D 08 29 54 2D 08  3D 54 2D 08 36 54 2D 08
```

Read 4 bytes in little endian (`0x082d541c`), subtract `0x08000000` from it, and get pointer `0x002d541c`. This is where the string actually starts.

Strings in ROM are terminated by one byte of 0x0. In cases where the translated string is longer than the original string, the script will write the translated string to the end of ROM (bytes `0x7fade0` ~ `0x7fffff` are unused), and then overwrite the pointer.

### Image

TODO

## Code structure

- `class TextBlock`: one `TextBlock` object corresponds to one `<text>` element in hikago.xml, and contains an array of `TextLine` objects.
- `class TextLine`: one `TextLine` object corresponds to one line of in-game text, and contains an array of java strings.
- `class Script`: the main class, it reads data from files, processes them, applies the translation to hikago.gba, and writes the result to _hikago.gba.
- `class ControlString`: one `ControlString` object corresponds to one special control string in hikago.xml, such as "@NAME@".