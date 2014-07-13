/*
 * Classe che permette di calcolare i WordVectors (tf) tramite lemmatizzazione o 
 * i word sense di BabelNet
 * 
 */
package sistcognes4;

import it.uniroma1.lcl.babelnet.BabelGloss;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetComparator;
import it.uniroma1.lcl.jlt.util.Language;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author daniele
 */
public class WordAnalisys {

    private HashSet<String> stopwords;
    private BabelNet bn = null;
    //risultati computazioni precedenti
    private HashMap<String, List<String>> preSense;
    private HashMap<String, List<List<String>>> preGloss;
    //connessione al db
    private Connection connect;
    private Statement statement;
    private final List<String> ANullo = new ArrayList<>();
    private final HashMap<String, Double> HNullo = new HashMap<>();

    public WordAnalisys() {
        try {
            //caricamento stopword
            BufferedReader stopfile = new BufferedReader(new FileReader("../stop_words/stop_it.txt"));
            stopwords = new HashSet<>();

            while (stopfile.ready()) {
                String linea = stopfile.readLine();
                int space = linea.indexOf(" ");
                if (space != -1) {
                    linea = linea.substring(0, space);
                }
                if (!linea.isEmpty()) {
                    stopwords.add(linea);
                }
            }

            //connessione al db
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://localhost/morphit?user=root&password=");
            statement = connect.createStatement();

        } catch (Exception e) {
            e.printStackTrace();
            stopwords = null;
        }

    }

    //WordVector sulla base di lemmi
    public List<String> termsByLemming(String frase) {
        if (frase.isEmpty()) {
            return ANullo;
        }
        try {

            ResultSet resultSet;
            String query;

            HashSet<String> hs = new HashSet<>();
            List<String> words = removeStopWords(frase);

            //lemmatizzazione
            query = "select lemma from morphit";
            if (words.size() > 0) {
                query += " where features like '%NOUN%' and form in ('" + words.get(0) + "'";
            }
            for (int j = 1; j < words.size(); j++) {
                query += ",'" + words.get(j) + "'";
            }
            query += ")";
            resultSet = statement.executeQuery(query);
            words.clear();
            while (resultSet.next() && !isStopWord(resultSet.getString("lemma"))) {
                hs.add(resultSet.getString("lemma").toLowerCase());
            }
            words.addAll(hs);

            return words;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //WordVector sulla base di lemmi e calcolo tf
    public HashMap<String, Double> termsByLemmingFreq(String frase) {
        if (frase.isEmpty()) {
            return HNullo;
        }
        try {

            ResultSet resultSet;
            String query;

            HashMap<String, Double> hs = new HashMap<>();
            List<String> words = removeStopWords(frase);

            int size = 0;
            //lemmatizzazione
            query = "select lemma from morphit";
            if (words.size() > 0) {
                query += " where features like '%NOUN%' and form in ('" + words.get(0) + "'";
            }
            for (int j = 1; j < words.size(); j++) {
                query += ",'" + words.get(j) + "'";
            }
            query += ")";
            resultSet = statement.executeQuery(query);
            words.clear();

            while (resultSet.next() && !isStopWord(resultSet.getString("lemma"))) {
                size++;
                double num = 0;
                if (hs.containsKey(resultSet.getString("lemma"))) {
                    num = hs.remove(resultSet.getString("lemma"));
                }
                hs.put(resultSet.getString("lemma"), num + 1);
            }
            //calcolo frequenza
            for (String k : hs.keySet()) {
                hs.put(k, hs.get(k) / size);
            }

            return hs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //WordVector sulla base di BabelSense e calcolo tf
    public HashMap<String, Double> termsBySenseFreq(String frase) {
        try {
            //avvio BabelNet e recupero possibili computazioni parziali
            if (bn == null) {
                bn = BabelNet.getInstance();
                preSense = new HashMap<>();
                preGloss = new HashMap<>();
                try {
                    ObjectInput input = new ObjectInputStream(new BufferedInputStream(new FileInputStream("sense.tmp")));
                    preSense = (HashMap<String, List<String>>) input.readObject();
                    input = new ObjectInputStream(new BufferedInputStream(new FileInputStream("gloss.tmp")));
                    preGloss = (HashMap<String, List<List<String>>>) input.readObject();

                } catch (Exception ex) {
                }
            }

            HashMap<String, Double> listTerms = tBLF(frase);
            int size = 0;
            HashMap<String, Double> ret = new HashMap<>();

            List<BabelSynset> synsets;
            List<Integer> scores = new ArrayList<>();
            List<BabelGloss> gloss;
            List<String> split;
            String g;
            String sense = "";
            double num;
            int winner, cwinner;
            List<String> tsenses;
            List<List<String>> tglosses;

            //per ogni termine della frase
            for (String termine : listTerms.keySet()) {

                scores.clear();
                winner = 0;
                cwinner = 0;
                size += listTerms.get(termine);
                //se non lo ho mai trattato
                if (!preSense.containsKey(termine)) {
                    //estraggo synsets
                    synsets = bn.getSynsets(Language.IT, termine);
                    try {
                        Collections.sort(synsets, new BabelSynsetComparator(termine));
                    } catch (IllegalArgumentException ext) {
                    }

                    tsenses = new ArrayList<>();
                    tglosses = new ArrayList<>();
                    //per ognuno estraggo i gloss
                    for (BabelSynset synset : synsets) {
                        tsenses.add(synset.getMainSense());
                        g = "";
                        gloss = synset.getGlosses(Language.IT);
                        if (gloss.size() > 0) {
                            g = gloss.get(0).getGloss();
                        }

                        //lemmatizzo tutto
                        split = termsByLemming(g);
                        tglosses.add(split);

                        int score = 0;
                        for (String s : split) {
                            if (listTerms.containsKey(s)) {
                                score++;
                            }
                        }
                        scores.add(score);
                        gloss.clear();

                    }

                    preSense.put(termine, tsenses);
                    preGloss.put(termine, tglosses);
                    synsets.clear();

                } else {
                    //algoritmo lesk per scegliere il senso migliore
                    for (List<String> glosses : preGloss.get(termine)) {
                        int score = 0;
                        for (String s : glosses) {
                            if (listTerms.containsKey(s)) {
                                score++;
                            }
                        }
                        scores.add(score);
                    }
                }

                //calcolo vincitore
                for (int j = 0; j < scores.size(); j++) {
                    if (cwinner < scores.get(j)) {
                        winner = j;
                        cwinner = scores.get(j);
                    }
                }

                //inserisco il vincitore
                if (preSense.get(termine).size() > 0) {

                    sense = preSense.get(termine).get(winner);
                    num = 0;
                    if (ret.containsKey(sense)) {
                        num = ret.get(sense);
                    }
                    ret.put(sense, listTerms.get(termine) + num);
                }



            }
            //calcolo tf
            for (String k : ret.keySet()) {
                ret.put(k, ret.get(k) / size);
            }

            savecaches();

            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    //rimozione Stopwords
    private List<String> removeStopWords(String frase) {
        try {
            frase = frase.replaceAll("([^a-zA-Zàéèìòù ])", " ").replaceAll("( )+", " ").trim().toLowerCase();
            List<String> split = new ArrayList<>();
            String[] sptemp = frase.split(" ");
            Collections.addAll(split, sptemp);

            for (int j = 0; j < sptemp.length; j++) {
                if (stopwords.contains(sptemp[j])) {
                    split.remove(sptemp[j]);
                }
            }

            return split;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private boolean isStopWord(String frase) {
        return stopwords.contains(frase);
    }

    //lemmatizzazione termini frase per BableNet (frequency)
    private HashMap<String, Double> tBLF(String frase) {
        if (frase.isEmpty()) {
            return HNullo;
        }
        try {

            ResultSet resultSet;
            String query;

            HashMap<String, Double> hs = new HashMap<>();
            List<String> words = removeStopWords(frase);
            
            query = "select lemma from morphit";
            if (words.size() > 0) {
                query += " where features like '%NOUN%' and form in ('" + words.get(0) + "'";
            }
            for (int j = 1; j < words.size(); j++) {
                query += ",'" + words.get(j) + "'";
            }
            query += ")";
            resultSet = statement.executeQuery(query);
            words.clear();

            while (resultSet.next() && !isStopWord(resultSet.getString("lemma"))) {
                
                double num = 0;
                if (hs.containsKey(resultSet.getString("lemma"))) {
                    num = hs.remove(resultSet.getString("lemma"));
                }
                hs.put(resultSet.getString("lemma"), num + 1);
            }

            return hs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //salvataggio computazioni parziali
    public void savecaches() {
        try {
            ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("sense.tmp")));
            output.writeObject(preSense);
            output.flush();
            output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("gloss.tmp")));
            output.writeObject(preGloss);
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
