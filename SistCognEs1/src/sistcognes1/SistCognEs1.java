/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sistcognes1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import rita.wordnet.RiWordnet;

/**
 *
 * @author daniele
 */
public class SistCognEs1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String termine="ash";
        
        ArrayList<String> frasi=new ArrayList<String>();
        frasi.add("The house was burnt to ashes while the owner returned.");
        frasi.add("This table is made of ash wood.");
        
        try{
            System.out.println(System.getProperty("user.dir"));
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
                //frase=frase.replaceAll(",","").replaceAll(";","").replace(".","");
                frase=frase.replaceAll("[^a-zA-Zàéèìòù ]"," ").replaceAll(" +"," ").trim();
                
                List<String> split= new ArrayList<String>();
                String[] sptemp=frase.toLowerCase().split(" ");
                Collections.addAll(split,sptemp);
                
                for(int i=0;i<sptemp.length;i++)
                    if(stopwords.contains(sptemp[i])) split.remove(sptemp[i]);
                
                ArrayList<List<String>> stems = getStems(split,wordnet);
                
                int termindex=0;
                for(int i=0;i<stems.size();i++)
                    if(stems.get(i).equals(Arrays.asList(wordnet.getStems(termine,RiWordnet.NOUN))))
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
                    String[] v=gl.replaceAll("([^a-zA-Zàéèìòù ])"," ").replaceAll("( )+"," ").trim().toLowerCase().split(" ");
                    List<String> tmp = new ArrayList<String>();
                    tmp.addAll(Arrays.asList(v));

                    for(int j=0;j<v.length;j++)
                        if(stopwords.contains(v[j])) tmp.remove(v[j]);
                    
                    for(String es:ex){
                        //v=es.replaceAll(",","").replaceAll(";","").replace(".","").split(" ");
                        v=es.replaceAll("[^a-zA-Zàéèìòù ]"," ").replaceAll(" +"," ").trim().toLowerCase().split(" ");
                        tmp.addAll(Arrays.asList(v));
                        for(int j=0;j<v.length;j++)
                            if(stopwords.contains(v[j])) tmp.remove(v[j]);
                    }
                    signature.add(tmp);
                    
                }
                ArrayList<List<List<String>>> stemSig = new ArrayList<List<List<String>>>();
                for(List<String> senso:signature)
                    stemSig.add(getStems(senso,wordnet));
                
                
                int[] counts = new int[numsense];
                for(int i=0;i<counts.length;i++){counts[i]=0;}
                
                
                for(int i=0;i<stemSig.size();i++){ 
                    for(int j=0;j<stemSig.get(i).size();j++){
                        int c=0;
                        for(String st:stemSig.get(i).get(j)){
                            for(List<String> s:stems)
                                if(s.indexOf(st)!=-1){ c=1; }  
                        }
                        counts[i]+=c;
                    }
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
    
    public static ArrayList<List<String>> getStems(List<String> split, RiWordnet wordnet){
        ArrayList<List<String>> poses = new ArrayList<List<String>>();
        for(String parola:split){
            poses.add(Arrays.asList(wordnet.getPos(parola)));
        }
            
        ArrayList<List<String>> stems = new ArrayList<List<String>>();
        for(int i=0;i<split.size();i++){
            List<String> temp = new ArrayList<String>();
        for(String pos:poses.get(i)){
            String[] t = wordnet.getStems(split.get(i),pos);
            if(t==null)t=new String[0];
            Collections.addAll(temp,t);
        }        
            HashSet hs = new HashSet();
            hs.addAll(temp);
            temp.clear();
            temp.addAll(hs);
            stems.add(temp);
            
        }
        return stems;
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
