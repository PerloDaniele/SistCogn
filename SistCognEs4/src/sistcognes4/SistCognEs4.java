/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

    static WordAnalisys wa = new WordAnalisys();
    static HashMap<String, HashMap<String, Double>> tf=null;
    static HashMap<Type, List<String>> profiles=null;
    static HashMap<String, Double> idf=null;
    static List<String> trainingdoc=null;
    static List<String> testdoc=null;

    static final String tfFile = "tfbabel.ser";
    //static final String tfFile = "tflemm.ser";
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // Calcolo e recupero dei documenti e costruzione profili
        listeDocumenti();
        tf();
        idf();
        profili();
        
        
        
        System.out.println("Finito");

    }
    
    public static void listeDocumenti(){
        trainingdoc=new ArrayList<>();
        testdoc=new ArrayList<>();
        File dir = new File("./docs_200/training");
        Collections.addAll(trainingdoc,dir.list());
        dir = new File("./docs_200/test");
        Collections.addAll(testdoc,dir.list());
    }
    
    public static void tf(){
        boolean caricamento = true;

        try {
            ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tfFile)));
            tf = (HashMap<String, HashMap<String, Double>>) input.readObject();
            System.out.println("tf caricati!");
            
        } catch (Exception e) {
            caricamento = false;
        }
        if (!caricamento) {
            tf = new HashMap<String, HashMap<String, Double>>();
            try {
                System.out.println("inizio apprendimento tf...");
                File trainingDir = new File("./docs_200/all");
                BufferedReader b;
                String content = "";

                HashSet<String> terms = new HashSet<String>();

                //per ogni documento
                for (String doc : trainingDir.list()) {
                    System.out.println("Documento: " + doc);
                    b = new BufferedReader(new FileReader("./docs_200/all/" + doc));

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
    
    public static void idf(){
        idf=new HashMap<>();
        int N=trainingdoc.size();
        for(String term:tf.get(trainingdoc.get(0)).keySet()){
            int n=0;
            for(String doc:trainingdoc){
                if(tf.get(doc).get(term)==0.0)
                    n++;
            }
            idf.put(term, (n!=0)? Math.log(N/n) : 0.0 );
        }
    }
    
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
}

enum Type {

    AMBIENTE, CINEMA, CUCINA, ECONOMIA, MOTORI, POLITICA, SALUTE, TECNOLOGIA, SPETTACOLI, SPORT
}
