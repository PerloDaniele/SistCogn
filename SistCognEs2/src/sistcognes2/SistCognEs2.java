/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sistcognes2;

 import java.util.*; 
 import edu.stanford.nlp.pipeline.*;
 import edu.stanford.nlp.ling.*; 
 import edu.stanford.nlp.ling.CoreAnnotations.*; 
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import rita.wordnet.RiWordnet;

/**
 *
 * @author daniele
 */
public class SistCognEs2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String termine="ash";
        
        ArrayList<String> frasi=new ArrayList<String>();
        frasi.add("The house was burnt to ashes while the owner returned.");
        frasi.add("This table is made of ash wood.");
        
        Properties props = new Properties(); 
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse"); 
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);
        
        
        try{
            
            BufferedReader stopfile = new BufferedReader(new FileReader("../stop_words/stop_words_FULL.txt"));
            ArrayList<String> stopwords = new ArrayList<String>();
            
            while(stopfile.ready())
                stopwords.add(stopfile.readLine());
            
            RiWordnet wordnet = new RiWordnet();    
            wordnet.setWordnetHome("/home/daniele/WordNet-3.0");
            
            List<String> posterm;
            posterm=Arrays.asList(wordnet.getPos(termine));
            
            for(String frase:frasi){
                String original=frase;
                //frase=frase.replaceAll(",","").replaceAll(";","").replace("\\.","");
                
                
                List<String> lems; 
                Annotation document = pipeline.process(frase);  
                frase="";
                for(CoreMap sentence: document.get(SentencesAnnotation.class)) {    
                    for(CoreLabel token: sentence.get(TokensAnnotation.class))     
                        frase += token.get(LemmaAnnotation.class) + " ";
                }
                
                List<String> split= new ArrayList<String>();
                //String[] sptemp=frase.replaceAll(",","").replaceAll(";","").replace("\\.","").trim().split(" ");
                String[] sptemp=frase.replaceAll("([^a-zA-Zàéèìòù ])"," ").replaceAll("( )+"," ").trim().toLowerCase().split(" ");
                Collections.addAll(split,sptemp);
                
                for(int i=0;i<sptemp.length;i++)
                    if(stopwords.contains(sptemp[i])) split.remove(sptemp[i]);
                
                lems = split;
                
                String lemmaterm=termine;
                document = pipeline.process(termine);  
                for(CoreMap sentence: document.get(SentencesAnnotation.class)) {    
                    for(CoreLabel token: sentence.get(TokensAnnotation.class))     
                        lemmaterm=token.get(LemmaAnnotation.class);
                }
                
                int termindex=0;
                for(int i=0;i<lems.size();i++)
                    if(lems.indexOf(lemmaterm)!=-1)
                        termindex=i;
                
                ArrayList<List<String>> signature = new ArrayList<List<String>>();
                int numsense = 0;
                List<Integer> ids = new ArrayList<Integer>();
                for(String pos:posterm){
                    int[] a = wordnet.getSenseIds(termine,pos);
                    for(int i=0;i<a.length;i++) ids.add(a[i]);
                }
                for(String pos:posterm){
                    int n=wordnet.getSenseCount(termine,pos);
                    numsense+=n;
                }
                for(int k=0;k<numsense;k++){
                    String gl=wordnet.getGloss(ids.get(k));
                    String[] e=wordnet.getExamples(ids.get(k));
                    if(e==null) e=new String[0];
                    List<String> ex=Arrays.asList(e);
                    
                    //String[] v=gl.replaceAll(",","").replaceAll(";","").replace(".","").split(" ");
                    List<String> tmp = new ArrayList<String>();
                    tmp.add(gl);
                    tmp.addAll(ex);
                    for(String l:tmp){
                    //String ss = "";
                    //for(String s:l) ss+=s+" ";
                    document = pipeline.process(l);  
                    String t = "";
                    for(CoreMap sentence: document.get(SentencesAnnotation.class)) {    
                        for(CoreLabel token: sentence.get(TokensAnnotation.class))    
                            t+=token.get(LemmaAnnotation.class) +" ";
                    }
                    
                    //String[] v=t.replaceAll(",","").replaceAll(";","").replace(".","").trim().split(" ");
                    String[] v=t.replaceAll("([^a-zA-Zàéèìòù ])"," ").replaceAll("( )+"," ").trim().toLowerCase().split(" ");
                    ArrayList<String> aus= new ArrayList<String>();
                    Collections.addAll(aus,v);
                    
                    for(int j=0;j<v.length;j++)
                        if(stopwords.contains(v[j])) aus.remove(v[j]);
                    
                    signature.add(aus);
                    }
                }
                //ArrayList<List<String>> lemSig = new ArrayList<List<String>>();
                
                
                int[] counts = new int[numsense];
                for(int i=0;i<counts.length;i++){counts[i]=0;}
                
                
                for(int i=0;i<signature.size();i++){ 
                    for(String s:lems)
                       if(signature.get(i).indexOf(s)!=-1)                        
                          counts[i]++;
                }
                
                int winner=getFirstSense(ids,wordnet);
                int cwinner=0;
                for(int i=0;i<counts.length;i++)
                    if(cwinner<counts[i]){winner=i;cwinner=counts[i];}
                
                System.out.println("Frase: "+ original);
                
                System.out.println("Sens of "+ termine+": " + wordnet.getGloss(ids.get(winner)));
            }
            
        
        }
        catch(Exception e ){e.printStackTrace();}
        
      }
    
        public static int getFirstSense(List<Integer> ids, RiWordnet wordnet){
        int first=0;
        int num=0;
        for(int k=0;k<ids.size();k++){
           String[] e= wordnet.getExamples(ids.get(k));
           if(e!=null && num<e.length)
           {num=e.length;first=k;}
        }
        
        return first;
    }
    }

