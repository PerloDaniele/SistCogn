/*
 * Creazione e interrogazione Triple Store
 */
package sistcognes5;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DC;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author daniele
 */
public class SistCognEs5 {

    
    static Model model = null;

    public static void main(String[] args) {

        caricaTripleStore();
        //ricerca su creatore
        queryCreator("Topo Gigio");
        //ricerca su descrizione
        queryDescription("Russia creates its own Silicon Valley.");
        queryDescription("Gianni mangia la mela");


    }

    //lettura collezione di documenti
    public static List<String> recuperoDocumenti() {
        ArrayList docs = new ArrayList<>();
        File dir = new File("./news_collection");
        Collections.addAll(docs, dir.list());
        return docs;
    }

    public static void caricaTripleStore() {
        boolean caricamento = true;

        try {
            model = ModelFactory.createDefaultModel();

            model.read(FileManager.get().open("./news_collection/news_collection.rdf"), null);
            //lettura triple store
            System.out.println("rdf caricato");

        } catch (Exception e) {
            caricamento = false;
        }
        if (!caricamento) {
            try {
                System.out.println("rdf inesistente, creazione...");

                //istanza Stanford
                Properties props = new Properties();
                props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
                StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);

                //caricamento stowords
                BufferedReader stopfile = new BufferedReader(new FileReader("../stop_words/stop_words_FULL.txt"));
                ArrayList<String> stopwords = new ArrayList<String>();
                while (stopfile.ready()) {
                    stopwords.add(stopfile.readLine());
                }

                List<String> docs = recuperoDocumenti();
                model = ModelFactory.createDefaultModel();
                BufferedReader buff;
                Resource res;
                String line, content, date;
                //aurore topogigio
                int topogigio = 2;

                //per ogni documento
                for (String doc : docs) {
                    buff = new BufferedReader(new FileReader("./news_collection/" + doc));
                    int countText = 0;
                    content = "";
                    date = "";
                    res = null;
                    String[] split;

                    while (buff.ready()) {
                        line = buff.readLine();
                        if (!line.trim().isEmpty() && line.charAt(0) == '#' && line.contains("http:")) {
                            split = line.trim().split(" ");
                            //creo la risorsa
                            res = model.createResource(split[split.length - 1]);
                        }
                        if (!line.trim().isEmpty() && line.charAt(0) != '#') {
                            countText++;
                            if (countText == 1) {
                                //aggiungo il titolo
                                res.addProperty(DC.title, line);
                                content = line;
                            } else if (countText == 2) {
                                //aggiungo descrizione
                                res.addProperty(DC.description, line);
                                content += " " + line;
                            } else {
                                content += " " + date;
                                date = line;
                            }
                        }
                    }
                    //aggiungo data e autore e publisher
                    res.addProperty(DC.date, date);
                    if (topogigio != 0) {
                        res.addProperty(DC.creator, "Topo Gigio");
                        topogigio--;
                    } else {
                        res.addProperty(DC.creator, "Daniele Perlo");
                    }

                    res.addProperty(DC.publisher, "BBC");

                    //lemmatizzazione del testo per la creazione del subject
                    Annotation document = pipeline.process(content);
                    String text = "";
                    for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
                        for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                            text += token.get(LemmaAnnotation.class) + " ";
                        }
                    }

                    List<String> words = new ArrayList<>();
                    String[] sptemp = text.replaceAll("([^a-zA-Zàéèìòù ])", " ").replaceAll("( )+", " ").trim().toLowerCase().split(" ");
                    Collections.addAll(words, sptemp);
                    //rimuovo stopwords
                    for (int i = 0; i < sptemp.length; i++) {
                        if (stopwords.contains(sptemp[i])) {
                            words.remove(sptemp[i]);
                        }
                    }

                    HashMap<String, Integer> hs = new HashMap<>();
                    for (String word : words) {
                        int num = 0;
                        if (hs.containsKey(word)) {
                            num = hs.remove(word);
                        }
                        hs.put(word, num + 1);
                    }
                    //prendo i 3 termini + frequenti
                    String[] subj = new String[3];
                    int primo = 0, secondo = 0, terzo = 0;
                    for (String word : hs.keySet()) {
                        if (hs.get(word) > primo) {
                            subj[2] = subj[1];
                            subj[1] = subj[0];
                            subj[0] = word;
                            terzo = secondo;
                            secondo = primo;
                            primo = hs.get(word);
                        } else if (hs.get(word) > secondo) {
                            subj[2] = subj[1];
                            subj[1] = word;
                            terzo = secondo;
                            secondo = hs.get(word);
                        } else if (hs.get(word) > terzo) {
                            subj[2] = word;
                            terzo = hs.get(word);
                        }
                    }
                    //aggiungo i subject
                    res.addProperty(DC.subject, subj[0] + ", " + subj[1] + ", " + subj[2]);

                }

                model.write(new FileOutputStream("./news_collection/news_collection.rdf"));
                System.out.println("rdf creato");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }




    }

    //creo la query per cercare tramite creatore
    public static void queryCreator(String creator) {
        String queryString = "PREFIX dc:  <http://purl.org/dc/elements/1.1/> SELECT ?doc ?title "
                + "WHERE { ?doc dc:creator \"" + creator + "\" . ?doc dc:title ?title .}";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            ResultSet results = qexec.execSelect();
            System.out.println("\nDocumenti di " + creator + "\n----");
            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();
                RDFNode x = soln.get("doc");
                Literal l = soln.getLiteral("title");   
                System.out.println("Document\n" + x + "\nTitle\n" + l + "\n-----");
            }
        } finally {
            qexec.close();
        }
    }

    //creo la query per cercare tramite descrizione
    public static void queryDescription(String title) {
        String queryString = "PREFIX dc:  <http://purl.org/dc/elements/1.1/> SELECT ?doc ?description "
                + "WHERE { ?doc dc:title \"" + title + "\" . ?doc dc:description ?description .}";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            ResultSet results = qexec.execSelect();
            System.out.println("\nDocumento \"" + title + "\"\n----");
            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();
                RDFNode x = soln.get("doc");
                Literal l = soln.getLiteral("description");
                System.out.println("Document\n" + x + "\nDescription\n" + l + "\n-----");
            }
        } finally {
            qexec.close();
        }
    }
}
