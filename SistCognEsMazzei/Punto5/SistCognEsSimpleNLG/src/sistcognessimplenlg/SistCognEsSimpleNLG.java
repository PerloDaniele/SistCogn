/*
 * Classe che implementa il modulo di Linguistic realization attraverso SimpleNLG
 */
package sistcognessimplenlg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import simplenlg.framework.*;
import simplenlg.lexicon.*;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.english.*;

public class SistCognEsSimpleNLG {

    //sentence plans e traduzioni
    final static String sentencePlan = "../../Punto4/sentence_plan";
    final static String translations = "../translations";

    public static void main(String[] args) throws IOException{

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String sentence = null;  
        Boolean formStdin = false;
        while( input.ready() && (sentence = input.readLine()) != null ) {
            formStdin = true;
            try{
            translate(sentence); 
            }catch(Exception e){System.out.println("Impossibilie leggere il SentencePlan");}
        } 
        if (!formStdin && args.length == 0) {
            try{
            translateAll();
            }catch(Exception e){System.out.println("Impossibilie leggere i SentencePlans");}
        } else if(!formStdin){
            try{
            translate(args[0]);
            }catch(Exception e){System.out.println("Impossibilie leggere il SentencePlan");}
        }

    }

    private static void translateAll() {
        Lexicon lexicon = Lexicon.getDefaultLexicon();
        NLGFactory nlgFactory = new NLGFactory(lexicon);
        Realiser realiser = new Realiser(lexicon);
        JSONParser parser = new JSONParser();

        try {
            BufferedReader b = new BufferedReader(new FileReader(sentencePlan));
            BufferedWriter w = new BufferedWriter(new FileWriter(translations));
            w.write("");

            while (b.ready()) {
                String line = b.readLine();

                SPhraseSpec p = nlgFactory.createClause();

				//lettura del JSON e estrazione degli elementi della frase
                JSONObject plan = (JSONObject) parser.parse(line);

                setVerb(plan, p);
                setSubj(plan, p, nlgFactory);
                setObj(plan, p, nlgFactory);
                setCterm(plan, p, nlgFactory);

                System.out.println(realiser.realiseSentence(p));
                w.append(realiser.realiseSentence(p));
                w.newLine();

            }
            w.flush();
            b.close();
            w.close();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void setVerb(JSONObject plan, SPhraseSpec p) {

        p.setVerb((String) plan.get("VERB"));

    }

    private static void setSubj(JSONObject plan, SPhraseSpec p, NLGFactory nlgFactory) {

		//recupero dei soggetti della frase
        JSONArray subjs = (JSONArray) plan.get("SUBJ");
        Iterator<JSONObject> it = subjs.iterator();
        JSONObject currentsubj;
        int c = 1;
        NPPhraseSpec s1, s2 = null;
        CoordinatedPhraseElement sCoord = null;
        do {
            currentsubj = it.next();
			//estrazione nome e quantificatore
            String q = (String) currentsubj.get("QUANT");
            String noun = (String) currentsubj.get("NOUN");
            if (q == null) {
                s1 = nlgFactory.createNounPhrase(noun);
            } else {
                if (q.equals("exist")) {
                    q = "a";
                }
                s1 = nlgFactory.createNounPhrase(q, noun);
            }
			//nel caso il nome fosse plurale
            if (((String) currentsubj.get("NUM")).equals("pl")) {
                s1.setPlural(true);
            } else {
                s1.setPlural(false);
            }
			//nel cao di + soggetti
            if (c > 1) {
                if (c > 1 && sCoord != null) {
                    sCoord.addCoordinate(s1);
                } else {
                    sCoord = nlgFactory.createCoordinatedPhrase(s1, s2);
                }
            }
            s2 = s1;
            c++;
        } while (c <= subjs.size());
        if (sCoord != null) {
            p.setSubject(sCoord);
        } else {
            p.setSubject(s1);
        }
    }

    private static void setObj(JSONObject plan, SPhraseSpec p, NLGFactory nlgFactory) {

		//lettura degli oggetti dal JSON
        JSONArray objs = (JSONArray) plan.get("OBJ");
        if (objs != null) {
            Iterator<JSONObject> it = objs.iterator();
            JSONObject currentobj;
            int c = 1;
            NPPhraseSpec o1, o2 = null;
            CoordinatedPhraseElement oCoord = null;
            do {
                currentobj = it.next();
                String q = (String) currentobj.get("QUANT");
                String noun = (String) currentobj.get("NOUN");
                if (q == null) {
                    o1 = nlgFactory.createNounPhrase(noun);
                } else {
                    if (q.equals("exist")) {
                        q = "a";
                    }
                    o1 = nlgFactory.createNounPhrase(q, noun);
                }
                if (((String) currentobj.get("NUM")).equals("pl")) {
                    o1.setPlural(true);
                } else {
                    o1.setPlural(false);
                }
				//nel caso di più oggetti
                if (c > 1) {
                    if (c > 1 && oCoord != null) {
                        oCoord.addCoordinate(o1);
                    } else {
                        oCoord = nlgFactory.createCoordinatedPhrase(o1, o2);
                    }
                }
                o2 = o1;
                c++;
            } while (c <= objs.size());
            if (oCoord != null) {
                p.setObject(oCoord);
            } else {
                p.setObject(o1);
            }
        }
    }

    private static void setCterm(JSONObject plan, SPhraseSpec p, NLGFactory nlgFactory) {

		//lettura del possibile complemento di termine dal JSON
        JSONObject jCTerm = (JSONObject) plan.get("CTERM");
        if (jCTerm != null) {
            NPPhraseSpec cterm;
            String qct = (String) jCTerm.get("QUANT");
            if (qct == null) {
                qct = "to";
            } else {
                if (qct.equals("exist")) {
                    qct = "a";
                }
                qct = "to " + qct;
            }
            cterm = nlgFactory.createNounPhrase(qct, (String) jCTerm.get("NOUN"));
            if (((String) jCTerm.get("NUM")).equals("pl")) {
                cterm.setPlural(true);
            } else {
                cterm.setPlural(false);
            }
            p.addModifier(cterm);
        }
    }

    private static void translate(String sentencePlan) {
        Lexicon lexicon = Lexicon.getDefaultLexicon();
        NLGFactory nlgFactory = new NLGFactory(lexicon);
        Realiser realiser = new Realiser(lexicon);
        JSONParser parser = new JSONParser();
        try {
            SPhraseSpec p = nlgFactory.createClause();

            JSONObject plan = (JSONObject) parser.parse(sentencePlan);

            setVerb(plan, p);
            setSubj(plan, p, nlgFactory);
            setObj(plan, p, nlgFactory);
            setCterm(plan, p, nlgFactory);

            System.out.println(realiser.realiseSentence(p));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
