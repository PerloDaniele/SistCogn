/*
 * Disambuguazione di un termine all'interno di un contesto utilizzando le definizioni di WordNet
 * attraverso il framework Rita
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

    
    public static void main(String[] args) {

	//termine da disambiguare
        String termine="ash";
        //contesti
        ArrayList<String> frasi=new ArrayList<String>();
        frasi.add("The house was burnt to ashes while the owner returned.");
        frasi.add("This table is made of ash wood.");
        
        try{
            //caricamento stopwords
            BufferedReader stopfile = new BufferedReader(new FileReader("../stop_words/stop_words_FULL.txt"));
            ArrayList<String> stopwords = new ArrayList<String>();
            
            while(stopfile.ready())
                stopwords.add(stopfile.readLine());
            //istanza di WordNet
            RiWordnet wordnet = new RiWordnet();    
            wordnet.setWordnetHome("/home/daniele/WordNet-3.0");
            
            List<String> posterm;
            posterm=Arrays.asList(wordnet.getPos(termine));
            
            for(String frase:frasi){

                String original=frase;

                frase=frase.replaceAll("[^a-zA-Zàéèìòù ]"," ").replaceAll(" +"," ").trim();
                //split frase
                List<String> split= new ArrayList<String>();
                String[] sptemp=frase.toLowerCase().split(" ");
                Collections.addAll(split,sptemp);
                //rimozione stopwords
                for(int i=0;i<sptemp.length;i++)
                    if(stopwords.contains(sptemp[i])) split.remove(sptemp[i]);
                
                ArrayList<List<String>> stems = getStems(split,wordnet);
                                
                //recupero gli id dei sensi per i Pos del termine
                ArrayList<List<String>> signature = new ArrayList<>();
                int numsense = 0;
                List<Integer> ids = new ArrayList<>();
                for(String pos:posterm){
                    int[] a = wordnet.getSenseIds(termine,pos);
                    for(int i=0;i<a.length;i++) ids.add(a[i]);
                }
                for(String pos:posterm){
                    int n=wordnet.getSenseCount(termine,pos);
                    numsense+=n;
                }
                //per ogni senso
                for(int k=0;k<numsense;k++){
                    String gl=wordnet.getGloss(ids.get(k));
                    String[] e=wordnet.getExamples(ids.get(k));
                    if(e==null) e=new String[0];
                    List<String> ex=Arrays.asList(e);
                    
                    //recupero gloss
                    String[] v=gl.replaceAll("([^a-zA-Zàéèìòù ])"," ").replaceAll("( )+"," ").trim().toLowerCase().split(" ");
                    List<String> tmp = new ArrayList<>();
                    tmp.addAll(Arrays.asList(v));

                    for(int j=0;j<v.length;j++)
                        if(stopwords.contains(v[j])) tmp.remove(v[j]);
                    
                    //recupero esempi
                    for(String es:ex){

                        v=es.replaceAll("[^a-zA-Zàéèìòù ]"," ").replaceAll(" +"," ").trim().toLowerCase().split(" ");
                        tmp.addAll(Arrays.asList(v));
                        for(int j=0;j<v.length;j++)
                            if(stopwords.contains(v[j])) tmp.remove(v[j]);
                    }
                    signature.add(tmp);
                    
                }
                //stemmatizzo tutto per ogni senso
                ArrayList<List<List<String>>> stemSig = new ArrayList<>();
                for(List<String> senso:signature)
                    stemSig.add(getStems(senso,wordnet));
                
                //algoritmo Lesk
                int[] counts = new int[numsense];
                for(int i=0;i<counts.length;i++){counts[i]=0;}
                
                //calcolo overlaps
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
                //cerco il vincitore
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
    
	//stemmatizzazione dei termini tramite il loro PoS
    public static ArrayList<List<String>> getStems(List<String> split, RiWordnet wordnet){
		//recupero PoS
        ArrayList<List<String>> poses = new ArrayList<>();
        for(String parola:split){
            poses.add(Arrays.asList(wordnet.getPos(parola)));
        }
        
		//recupero stemmi
        ArrayList<List<String>> stems = new ArrayList<>();
        for(int i=0;i<split.size();i++){
            List<String> temp = new ArrayList<>();
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
    
	//senso statisticamente più probabile (in base agli esempi)
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
