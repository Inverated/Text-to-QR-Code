# Text to Scannable Code
Convert any text to scannable codes without 3rd party redirect.
Using OpenCV to process image and Zxing library to code and decode.
Compilled using maven.

Note: The code is done very badly before I knew of the concept of OOP. Might return to redo 

# Required Packages
1. JavaFx
2. OpenCV
*Add bin and lib folders to environment path

# How to encode
1. Run App.java / clone and run ```mvn javafx:run ```
2. Fill up value to encode
3. Select encode type
4. Customise color (Add logo for QR Code)
5. Generate the code
6. Download with download button
*Before downloading will scan the qr code to see if it is still scannable after the addition of logo*

# How to decode
1. Run App.java / clone and run ```mvn javafx:run ```
2. Upload image / open camera (right click to change camera)
3. Wait/move image around (Using camera sucks in detecting)
4. Should work with tilted / slanted image
5. If successful, will automatically fill up result on top

## Notes
1. First time making GUI, java, VScode, javafx, maven... Basically first time for the whole thing. This project is basically meant to learn java.
2. Decoding of codes is not good with Zxing library. Small issues can make it unreadable:
- A bit far
- Slightly tilted
- Not flat
3. ~~Planned to use OpenCV to fix the scanning issue but tried forever to import OpenCV into maven. Nothing works...~~
4. I want to go back to python

## Future Plans (Very far in future. Need catch up in school work)
1. Shortcut to generate unique codes for wifi passowords etc.
2. ~~Make an executable~~ (no idea how, nothing works, even when file made its ridiculiously big and does not work) (Still does not work. gave up)
3. ~~Get OpenCV to work~~
4. ~~Add a way to use camera to scanning~~
5. Maybe try making an android app
6. Make a python version (i miss pip install)

   
