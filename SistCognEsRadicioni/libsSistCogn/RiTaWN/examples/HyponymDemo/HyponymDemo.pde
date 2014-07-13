import rita.wordnet.*;

String word = "cat";
String pos = "n";

RiWordnet wordnet = new RiWordnet(this);

size(1000, 770);
background(245);
textFont(createFont("Arial", 12));

fill(0);
float xPos=40, yPos=5;
int[] ids = wordnet.getSenseIds(word, pos);
text("====================== getSenseIds('cat', 'n'):", xPos, yPos+=20);
text("getDescription(id) ============================", xPos+280, yPos);
yPos += 8;
for (int j = 0; j < ids.length; j++) {
  text("#"+ids[j]+":   ", xPos, yPos+=15);
  text(wordnet.getDescription(ids[j]), xPos+80, yPos);
}


xPos=20; 
yPos=190;
String[] result = wordnet.getHyponyms(word, pos);
text("======== getHyponyms('cat', 'n') =======", xPos, yPos);
for (int i = 0; i < result.length; i++)
  text("(first-sense)      "+result[i], xPos+57, yPos+=15);


result = wordnet.getAllHyponyms(word, pos);
text("====== getAllHyponyms('cat', 'n') ======", xPos, yPos+=40);
text("(all-senses)      ", xPos+=57, yPos+=20);
yPos += 8;
for (int i = 0; i < result.length; i++)
  text(result[i], xPos+80, yPos+=15);


xPos=325; 
yPos=190;
text("======== getHyponyms(id) =======", xPos, yPos);
for (int j = 0; j < ids.length; j++) {
  result = wordnet.getHyponyms(ids[j]); 
  text("#"+ids[j]+": ", xPos, yPos+=15);
  if (result == null) continue; 
  for (int i = 0; i < result.length; i++)
    text(result[i], xPos+110, yPos+=15);
}


xPos = 600; 
yPos=190;
text("======== getHyponymTree(id) ======", xPos, yPos);
for (int j = 0; j < ids.length; j++) {
  result = wordnet.getHyponymTree(ids[j]);
  if (result == null) continue;
  text("#"+ids[j]+":", xPos, yPos+=15);
  for (int i = 0; i < result.length; i++)
    text(result[i], xPos+100, yPos+=15);
}
