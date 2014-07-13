/*
 * WSD
 * Disambiguazione termini usando WordNet
 * Utilizzando Stanford CoreNLP con lemmatizzazione
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

    public static void main(String[] args) {

        //termine da disambiguare
        String termine = "ash";

        //contesti
        ArrayList<String> frasi = new ArrayList<>();
        frasi.add("The house was burnt to ashes while the owner returned.");
        frasi.add("This table is made of ash wood.");

        //istanza StanfordCoreNLP
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);


        try {
            //lettura stopwords
            BufferedReader stopfile = new BufferedReader(new FileReader("../stop_words/stop_words_FULL.txt"));
            ArrayList<String> stopwords = new ArrayList<>();

            while (stopfile.ready()) {
                stopwords.add(stopfile.readLine());
            }

            //istanza wordnet
            RiWordnet wordnet = new RiWordnet();
            wordnet.setWordnetHome("/home/daniele/WordNet-3.0");
            //pos del termine
            List<String> posterm;
            posterm = Arrays.asList(wordnet.getPos(termine));

            for (String frase : frasi) {
                String original = frase;

                //recupero lemmi da Stanford
                List<String> lems;
                Annotation document = pipeline.process(frase);
                frase = "";
                for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
                    for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                        frase += token.get(LemmaAnnotation.class) + " ";
                    }
                }

                List<String> split = new ArrayList<>();
                String[] sptemp = frase.replaceAll("([^a-zA-Zàéèìòù ])", " ").replaceAll("( )+", " ").trim().toLowerCase().split(" ");
                Collections.addAll(split, sptemp);
                //rimozione stopwords
                for (int i = 0; i < sptemp.length; i++) {
                    if (stopwords.contains(sptemp[i])) {
                        split.remove(sptemp[i]);
                    }
                }

                lems = split;

                //recupero sensi dai pos del termine
                ArrayList<List<String>> signature = new ArrayList<>();
                int numsense = 0;
                List<Integer> ids = new ArrayList<>();
                for (String pos : posterm) {
                    int[] a = wordnet.getSenseIds(termine, pos);
                    for (int i = 0; i < a.length; i++) {
                        ids.add(a[i]);
                    }
                }
                for (String pos : posterm) {
                    int n = wordnet.getSenseCount(termine, pos);
                    numsense += n;
                }
                for (int k = 0; k < numsense; k++) {
                    //per ogni senso recupero esempi e gloss e lemmatizzo tutto
                    String gl = wordnet.getGloss(ids.get(k));
                    String[] e = wordnet.getExamples(ids.get(k));
                    if (e == null) {
                        e = new String[0];
                    }
                    List<String> ex = Arrays.asList(e);

                    List<String> tmp = new ArrayList<>();
                    tmp.add(gl);
                    tmp.addAll(ex);
                    for (String l : tmp) {
                        document = pipeline.process(l);
                        String t = "";
                        for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
                            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                                t += token.get(LemmaAnnotation.class) + " ";
                            }
                        }

                        String[] v = t.replaceAll("([^a-zA-Zàéèìòù ])", " ").replaceAll("( )+", " ").trim().toLowerCase().split(" ");
                        ArrayList<String> aus = new ArrayList<>();
                        Collections.addAll(aus, v);

                        for (int j = 0; j < v.length; j++) {
                            if (stopwords.contains(v[j])) {
                                aus.remove(v[j]);
                            }
                        }

                        signature.add(aus);
                    }
                }

                //algoritmo Lesk
                int[] counts = new int[numsense];
                for (int i = 0; i < counts.length; i++) {
                    counts[i] = 0;
                }


                for (int i = 0; i < signature.size(); i++) {
                    for (String s : lems) {
                        if (signature.get(i).indexOf(s) != -1) {
                            counts[i]++;
                        }
                    }
                }
                
                //cerco il vincitore
                int winner = getFirstSense(ids, wordnet);
                int cwinner = 0;
                for (int i = 0; i < counts.length; i++) {
                    if (cwinner < counts[i]) {
                        winner = i;
                        cwinner = counts[i];
                    }
                }

                System.out.println("Frase: " + original);

                System.out.println("Sens of " + termine + ": " + wordnet.getGloss(ids.get(winner)));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //recupera il sento statisticamente + probabile
    public static int getFirstSense(List<Integer> ids, RiWordnet wordnet) {
        int first = 0;
        int num = 0;
        for (int k = 0; k < ids.size(); k++) {
            String[] e = wordnet.getExamples(ids.get(k));
            if (e != null && num < e.length) {
                num = e.length;
                first = k;
            }
        }

        return first;
    }
}
