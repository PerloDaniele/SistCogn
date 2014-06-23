/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sistcognes3;

import java.sql.Connection;
import it.uniroma1.lcl.babelnet.BabelGloss;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetComparator;
import it.uniroma1.lcl.jlt.util.Language;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import edu.mit.jwi.item.IPointer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author daniele
 */
public class SistCognEs3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        ArrayList<String> termini = new ArrayList<>();
        termini.add("pianta");
        termini.add("testa");
        
        ArrayList<ArrayList<String>> frasi=new ArrayList<>();
        frasi.add(new ArrayList<String>());
        frasi.get(0).add("La pianta dell'alloggio è disponibile in ufficio, accanto all'appartamento; dal disegno è possibile cogliere i dettagli dell'architettura dello stabile, sulla distribuzione dei vani e la disposizione di porte e finestre.");
        frasi.get(0).add("I platani sono piante ad alto fusto, organismi viventi: non ha senso toglierli per fare posto a un parcheggio.");
        frasi.add(new ArrayList<String>());
        frasi.get(1).add("Si tratta di un uomo facilmente riconoscibile: ha la testa piccola, gli occhi sporgenti, il naso adunco e piccole orecchie a sventola.");
        frasi.get(1).add("Come per tutte le cose, ci vorrebbe un po' di testa, un minimo di ragione, una punta di cervello, per non prendere decisioni fuori dal senso dell'intelletto.");
        
        try{
                    
        BufferedReader stopfile = new BufferedReader(new FileReader("../stop_words/stop_it.txt"));
        HashSet<String> stopwords = new HashSet<String>();
            
        while(stopfile.ready()){
            String linea = stopfile.readLine();
            int space = linea.indexOf(" ");
            if(space!=-1)
                linea=linea.substring(0, space);
            if(!linea.isEmpty()) stopwords.add(linea);
        }
        
        
        BabelNet bn = BabelNet.getInstance();
        
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection("jdbc:mysql://localhost/morphit?user=root&password=");
        Statement statement = connect.createStatement();
        ResultSet resultSet;
        
        List<BabelSynset> synsets;
        List<List<BabelSense>> senseIT = new ArrayList<>();
        List<HashMap<String,Integer>> glossIT = new ArrayList<HashMap<String,Integer>>();
        HashSet hs=new HashSet();
        List<BabelGloss> glosses = new ArrayList<>();
        List<BabelSense> temp;
        Map<IPointer, List<BabelSynset>> relatedSynsets;
        List<BabelSynset> relationSynsets;
        List<String> splitGloss= new ArrayList<>();
        String paroleGloss;
        String query;
        List<Integer> scores= new ArrayList<>();
        List<Integer> numGloss = new ArrayList<>();
        HashMap<String,Integer> nounGloss = new HashMap<>();
        
        for(int i=0;i<termini.size();i++){              
                
                System.out.println("\n\ninizio termine: " + termini.get(i));
                synsets = bn.getSynsets(Language.IT, termini.get(i));
                Collections.sort(synsets, new BabelSynsetComparator(termini.get(i)));
                
                
                senseIT.clear();
                glossIT.clear();
                numGloss.clear();
                int c=0;
		for (BabelSynset synset : synsets)
		{   
                    
                    System.out.println("Senso n: " + c);
                    c++;
                    
                    glosses.clear();
                    temp = new ArrayList<>();
                    for (BabelSense sense : synset.getSenses(Language.IT))
                        if(sense.getLanguage()==Language.IT)
                            temp.add(sense);
                    senseIT.add(temp);
                                
                    for (BabelGloss gloss : synset.getGlosses(Language.IT))
                        if(gloss.getLanguage()==Language.IT)
                            glosses.add(gloss);
                    
                    
                    nounGloss = new HashMap<>();
                    
                    System.out.println("inizio related: syn "+synset.toString()+", termine: " + termini.get(i));
                    relatedSynsets = synset.getRelatedMap(); 
                    for (IPointer relationType : relatedSynsets.keySet())
                    {
                        
                        if(relationType.getName().equals("Hypernym")){
                        relationSynsets = relatedSynsets.get(relationType);
			for (BabelSynset relationSynset : relationSynsets)
			{
                            for (BabelGloss gloss : relationSynset.getGlosses(Language.IT))
                                if(gloss.getLanguage()==Language.IT && !glosses.contains(gloss))
                                    glosses.add(gloss);
                        }
                        relationSynsets.clear();
                        }
                    }
                    relatedSynsets.clear();
                    System.out.println("end related");
                    paroleGloss="";
                    System.out.println("numero gloss: "+glosses.size());
                    numGloss.add(glosses.size());
                    
                    for(BabelGloss gloss : glosses){
                         paroleGloss +=" "+ gloss.getGloss().toLowerCase();
                    }
                    
                    splitGloss.clear();
                    
                    String[] sptempGloss=paroleGloss.replaceAll("([^a-zA-Zàéèìòù ])"," ").replaceAll("( )+"," ").trim().split(" ");
                    Collections.addAll(splitGloss,sptempGloss);
                    
                    System.out.println("rimozione stop word");
                    for(int j=0;j<sptempGloss.length;j++)
                        if(stopwords.contains(sptempGloss[j])) splitGloss.remove(sptempGloss[j]);
                                           
                    System.out.println("inizio lemmatizzazione");
                    query="select distinct lemma, features from morphit";
                    if(splitGloss.size()>0) query+=" where form in ('"+splitGloss.get(0)+"'";
                    for(int j=1;j<splitGloss.size();j++)
                        query+=",'"+splitGloss.get(j)+"'";
                    query+=")";
                    resultSet = statement.executeQuery(query);
                    splitGloss.clear();
                    
                    while(resultSet.next()){
                        int num=0;
                        if(resultSet.getString("features").contains("NOUN")){
                            if(nounGloss.containsKey(resultSet.getString("lemma")))
                                num = nounGloss.remove(resultSet.getString("lemma"));
                            nounGloss.put(resultSet.getString("lemma"), num+1);
                        }
                    }    
                    
                    System.out.println("fine lemmatizzazione");
                    System.out.println("numero parole gloss: "+splitGloss.size());
                 
                    glossIT.add(nounGloss);
                }
                
            System.out.println("\n\nanalisi per termine "+termini.get(i));
            scores.clear();
            for(String frase:frasi.get(i)){
                
                
                String original=frase;
                frase=frase.replaceAll("([^a-zA-Zàéèìòù ])"," ").replaceAll("( )+"," ").trim().toLowerCase();
                List<String> split= new ArrayList<>();
                String[] sptemp=frase.split(" ");
                Collections.addAll(split,sptemp);
                
                for(int j=0;j<sptemp.length;j++)
                    if(stopwords.contains(sptemp[j])) split.remove(sptemp[j]);
                
                hs.clear();
                query="select lemma from morphit";
                if(split.size()>0) query+=" where form in ('"+split.get(0)+"'";
                for(int j=1;j<split.size();j++)
                    query+=",'"+split.get(j)+"'";
                query+=")";
                resultSet = statement.executeQuery(query);
                split.clear();
                hs.clear();
                while(resultSet.next())
                    hs.add(resultSet.getString("lemma").toLowerCase());
                split.addAll(hs);    
                
                System.out.println("\n\ncalcolo score...");
                scores.clear();
                for(int j=0;j<synsets.size();j++){
                      int score=0;
                      for(String parola:split) 
                         if(glossIT.get(j).containsKey(parola))
                            score += glossIT.get(j).get(parola);
                    scores.add(score);
                }
                
                int winner=getFirstSense(numGloss);
                int cwinner=0;
                for(int j=0;j<scores.size();j++)
                    if(cwinner<scores.get(j)){winner=j;cwinner=scores.get(j);}
                
                System.out.println("Termine : " + termini.get(i) +"\nFrase: "+original+"\nSenso: " + synsets.get(winner).getGlosses(Language.IT).get(0).getGloss());//senseIT.get(winner));
            }
        }
        
        }catch(Exception e){e.printStackTrace();}
    }
    
     public static int getFirstSense(List<Integer> numGloss){
        int first=0;
        int num=0;
        for(int k=0;k<numGloss.size();k++){
           if(num<numGloss.get(k))
           {num=numGloss.get(k);first=k;}
        }
        
        return first;
    }
}
