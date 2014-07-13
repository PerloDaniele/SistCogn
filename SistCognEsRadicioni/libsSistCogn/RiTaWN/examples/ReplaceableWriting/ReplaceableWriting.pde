import rita.*; 
import rita.wordnet.*;

// Note: this example also requires the core RiTa library

RiText[] rts; 
RiWordnet wordnet; 
int timestamp = 0, charsPerLine = 60;
String test = "Processing is an open source programming language and environment for people who want to program images, animation, and interactions. It is used by students, artists, designers, researchers, and hobbyists for learning, prototyping, and production. It is created to teach fundamentals of computer programming within a visual context and to serve as a software sketchbook and professional production tool. Processing is developed by artists and designers as an alternative to proprietary software tools in the same domain.";

void setup()
{
  size(500, 350);  
  wordnet = new RiWordnet(this);
  rts = RiText.createLines(this, test, 30, 50, charsPerLine);    
}

void draw()
{
  background(255);

  // substitute every 1/2 second...
  if (millis()-timestamp > 500)      
    doSubstitution();
}

/*  replace a random word in the paragraph with one
 from wordnet with the same (basic) part-of-speech */
void doSubstitution()
{
  String[] words = test.split(" ");

  int count =  (int)random(0, words.length);
  for (int i = count; i < words.length; i++) // loop from a random spot
  {
    String pos = wordnet.getBestPos(words[i].toLowerCase());        
    if (pos != null) 
    {
      String[] syns = wordnet.getSynset(words[i], pos);
      if (syns == null) continue;

      String newStr = syns[(int)random(0, syns.length)];

      if (Character.isUpperCase(words[i].charAt(0)))              
        newStr = firstUpperCase(newStr); // maintain capitalization

      test = RiText.regexReplace("\\b"+words[i]+"\\b", test, newStr);

      RiText.deleteAll();   // clean up the last batch

      // create a RiText[] from 'test' starting at (30,50) & going down
      rts = RiText.createLines(this, test, 30, 50, charsPerLine);
      break;
    }
    if (count == words.length) count = 0;
  }       
  timestamp = millis();
}


String firstUpperCase(String newStr)
{
  char fc = newStr.charAt(0);
  String tmp = newStr.substring(1);
  return Character.toUpperCase(fc)+tmp;
}