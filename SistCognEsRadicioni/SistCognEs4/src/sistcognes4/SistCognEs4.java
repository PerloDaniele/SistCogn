/*
 * 
 * Riconoscimento del contenuto dei documenti tramite la creazione di profili
 * Si utilizza la cosine similarity come similarità tra i word vectors
 * e si utilizzano centroidi classici, di Rocchio (Ottimizzati e non) come base 
 * per il profiling, costruiti in seguito al training by examples
 * 
 * Si utilizzano word vectors utilizzano lemmi oppure i sense di BabelNet
 * 
 */
package sistcognes4;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author daniele
 */
public class SistCognEs4 {

    //classe che precalcola i tf
    static WordAnalisys wa;
    //tf
    static HashMap<String, HashMap<String, Double>> tf=null;
    //tfidf
    static HashMap<String, HashMap<String, Double>> tfidf=null;
    //lista profili
    static HashMap<Type, List<String>> profiles=null;
    //idf
    static HashMap<String, Double> idf=null;
    //documenti per il training
    static List<String> trainingdoc=null;
    //documenti di test
    static List<String> testdoc=null;
    //vari centroidi
    static HashMap<Type, HashMap<String, Double>> centroidi=null;
    static HashMap<Type, HashMap<String, Double>> centroidiRocchio=null;
    static HashMap<Type, HashMap<String, Double>> centroidiRocchioMigliorato=null;
    //parametri per i centroidi calcolati con il metodo di Rocchio
    static final int beta=16;
    static final int gamma=4;
    //sources dei doc
    static final String training = "./docs_200/training";
    static final String test = "./docs_200/test";
    static final String all = "./docs_200/all";
    
    //IMPORTANTE: scommentare per il tipo di tf desiderato
    //tf sulla base dei WordSense
    //static final String tfFile = "tfbabel.ser";
    //tf sulla base dei lemmi
    static final String tfFile = "tflemm.ser";
    
    
    public static void main(String[] args) {
        
        // Calcolo e recupero dei documenti e costruzione profili
        listeDocumenti();
        //caricamento tf
        tf();
        //calcolo idf sui doc di training, caricamento profili e calcolo tfidf
        idf();
        profili();
        tfidf();
        
        //calcolo die vari centroidi
        centroidi();
        centroidiRocchio();
        centroidiRocchioMigliorato();
        
        //test sui documenti di test
        testCentroidi();
        testCentroidiRocchio();
        testCentroidiRocchioMigliorato();
        
        System.out.println("Finito");

    }
    
    //carica le liste dei documenti da utilizzare
    public static void listeDocumenti(){
        trainingdoc=new ArrayList<>();
        testdoc=new ArrayList<>();
        File dir = new File(training);
        Collections.addAll(trainingdoc,dir.list());
        dir = new File(test);
        Collections.addAll(testdoc,dir.list());
        Collections.sort(testdoc);
    }
    
    //caricamento WordVectors (tf)
    public static void tf(){
        boolean caricamento = true;

        try {
            //carico da file
            ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tfFile)));
            tf = (HashMap<String, HashMap<String, Double>>) input.readObject();
            System.out.println("tf caricati!");
            
        } catch (Exception e) {
            caricamento = false;
        }
        if (!caricamento) {
            //se il file non esiste calcolo i WordVectors
            tf = new HashMap<>();
            try {
                wa = new WordAnalisys();
                System.out.println("inizio apprendimento tf...");
                File trainingDir = new File(all);
                BufferedReader b;
                String content = "";

                HashSet<String> terms = new HashSet<>();

                //per ogni documento
                for (String doc : trainingDir.list()) {
                    System.out.println("Documento: " + doc);
                    b = new BufferedReader(new FileReader(all + doc));

                    //contenuto come sequenza di caratteri
                    content = "";
                    while (b.ready()) {
                        content += " " + b.readLine();
                    }

                    //estrazione termini con tf
                    if (tfFile.contains("lemm")) {
                        tf.put(doc, wa.termsByLemmingFreq(content));
                    } else {
                        tf.put(doc, wa.termsBySenseFreq(content));
                    }

                    terms.addAll(tf.get(doc).keySet());

                    b.close();
                }

                System.out.println("Riscrittura document vectors...");
                //riscrittura document vectors completi
                HashMap<String, Double> vector;
                
                for (String doc : tf.keySet()) {
                    vector = tf.get(doc);
                    for (String term : terms) {
                        if (!vector.containsKey(term)) 
                            vector.put(term, 0.0);
                    }
                    tf.put(doc, (HashMap<String, Double>) vector.clone());
                    vector.clear();
                }

                ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tfFile)));
                output.writeObject(tf);
                output.flush();
                System.out.println("tf calcolati e salvati!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    
    }
    
    //calcolo idf
    public static void idf(){
        idf=new HashMap<>();
        int N=trainingdoc.size();
        for(String term:tf.get(trainingdoc.get(0)).keySet()){
            int n=0;
            for(String doc:trainingdoc){
                if(tf.get(doc).get(term)!=0.0)
                    n++;
            }
            idf.put(term, (n!=0)? Math.log(N/n) : 0.0 );
        }
    }
    //caricamento profili (tramite nome)
    public static void profili(){
        profiles = new HashMap<>();
        List<String> aus;
        for (Type t : Type.values()) {
            profiles.put(t, new ArrayList<String>());
        }
        for(String doc:trainingdoc)
            for(Type t:Type.values())
                if (doc.toUpperCase().contains(t.name())) {
                    aus = profiles.get(t);
                    aus.add(doc);
                    profiles.put(t, aus);
                }
    }
    
    //calcolo tfidf
    public static void tfidf(){
        tfidf = new HashMap<>();
        HashMap<String, Double> aus;
        HashMap<String, Double> tfdoc;
        for(String doc : trainingdoc){
            tfdoc = tf.get(doc);
            aus = new HashMap<>();
            for(String term : tfdoc.keySet()){
                aus.put(term, tfdoc.get(term)*idf.get(term));
            }
            tfidf.put(doc, aus);
        }
        
    }
    
    //calcolo centroidi tradizionali
    public static void centroidi(){
        centroidi = new HashMap<>();
        HashMap<String,Double> aus;
        //per ogni profilo
        for (Type t : Type.values()) {
            aus=new HashMap<>();
            for(String doc : profiles.get(t)){
               for(String term : tfidf.get(doc).keySet()){
               
                   if(aus.containsKey(term)) 
                       aus.put(term, aus.get(term) + tfidf.get(doc).get(term));
                   else
                       aus.put(term, tfidf.get(doc).get(term));
               
               } 
            }
            //media dei valori del wordvector tra i componenti del profilo
            for(String term : aus.keySet()){
                aus.put(term, aus.get(term)/profiles.get(t).size());
            }
            centroidi.put(t,aus);
        }
        
    }
    
    public static void centroidiRocchio(){
        centroidiRocchio = new HashMap<>();
        HashMap<String,Double> aus;
        //per ogni profilo
        for (Type t : Type.values()) {
            aus=new HashMap<>();
            for(String doc : profiles.get(t)){
               for(String term : tfidf.get(doc).keySet()){
               
                   if(aus.containsKey(term)) 
                       aus.put(term, aus.get(term) + tfidf.get(doc).get(term));
                   else
                       aus.put(term, tfidf.get(doc).get(term));
               
               } 
            }
            //calcolo del wordvector come somma pesata tra i valori dei documenti 
            //appartenenti al profilo e non
            for(String term : aus.keySet()){
                aus.put(term, beta*aus.get(term)/profiles.get(t).size() - gamma*aus.get(term)/(trainingdoc.size()-profiles.get(t).size()));
            }
            centroidiRocchio.put(t,aus);
        }
    }
    
    public static void centroidiRocchioMigliorato(){
        centroidiRocchioMigliorato = new HashMap<>();
        HashMap<String,Double> aus;
        //per ogni profilo
        for (Type t : Type.values()) {
            aus=new HashMap<>();
            for(String doc : profiles.get(t)){
               for(String term : tfidf.get(doc).keySet()){
               
                   if(aus.containsKey(term)) 
                       aus.put(term, aus.get(term) + tfidf.get(doc).get(term));
                   else
                       aus.put(term, tfidf.get(doc).get(term));
               
               } 
            }
            //calcolo del wordvector come somma pesata tra i valori dei documenti 
            //appartenenti al profilo e near positive
            //il profilo "near" e' quello con cosine similarity maggiore tra i
            //centroidi classici
            Type NP = Type.AMBIENTE;
            Double maxsim = Double.MIN_VALUE;
            for(Type tnp:Type.values())
                if(tnp!=t){
                    Double s = cosSimilarity(centroidi.get(t),centroidi.get(tnp));
                    if(s>maxsim){ maxsim=s; NP=tnp; }
                }

            for(String term : aus.keySet()){
                aus.put(term, beta*aus.get(term)/profiles.get(t).size() - gamma*aus.get(term)/(profiles.get(NP).size()));
            }
            centroidiRocchioMigliorato.put(t,aus);
        }
    }
    
    //implementazione della cosine similarity
    public static Double modulo ( HashMap<String,Double> v){
    
        Double res=0.0;
        for(String i : v.keySet()){
            res+=v.get(i)*v.get(i);
        }
        return Math.sqrt(res);
    }
    
    public static Double prodScalare ( HashMap<String,Double> v1,HashMap<String,Double> v2){
    
        Double res=0.0;
        for(String i : v1.keySet()){
            res+=v1.get(i)*v2.get(i);
        }
        return res;
    }
    
    public static Double cosSimilarity(HashMap<String,Double> v1,HashMap<String,Double> v2){
        return prodScalare(v1,v2)/(modulo(v1)*modulo(v2));
    }
    
    
    //Test sui vari tipi di classificazione utilizzando la cosine similarity
    public static void testCentroidi(){
        System.out.println("***Centroidi tradizionali***");
        for(String doc:testdoc){
            Type NP = Type.AMBIENTE;
            Double maxsim = Double.MIN_VALUE;
            for(Type tnp:Type.values()){
                Double s = cosSimilarity(tf.get(doc),centroidi.get(tnp));
                if(s>maxsim){ maxsim=s; NP=tnp; }
            }
            System.out.println(doc+" -> " + NP);
        }
        System.out.println("*** END ***\n");
        
    }
    
    public static void testCentroidiRocchio(){
        System.out.println("***Centroidi Rocchio***");
        for(String doc:testdoc){
            Type NP = Type.AMBIENTE;
            Double maxsim = Double.MIN_VALUE;
            for(Type tnp:Type.values()){
                Double s = cosSimilarity(tf.get(doc),centroidiRocchio.get(tnp));
                if(s>maxsim){ maxsim=s; NP=tnp; }
            }
            System.out.println(doc+" -> " + NP);
        }
        System.out.println("*** END ***\n");
        
    }
    
    public static void testCentroidiRocchioMigliorato(){
        System.out.println("***Centroidi Rocchio Migliorato***");
        for(String doc:testdoc){
            Type NP = Type.AMBIENTE;
            Double maxsim = Double.MIN_VALUE;
            for(Type tnp:Type.values()){
                Double s = cosSimilarity(tf.get(doc),centroidiRocchioMigliorato.get(tnp));
                if(s>maxsim){ maxsim=s; NP=tnp; }
            }
            System.out.println(doc+" -> " + NP);
        }
        System.out.println("*** END ***\n");
        
    }
}

enum Type {

    AMBIENTE, CINEMA, CUCINA, ECONOMIA, MOTORI, POLITICA, SALUTE, TECNOLOGIA, SPETTACOLI, SPORT
}
