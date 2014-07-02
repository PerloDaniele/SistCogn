/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sistcognessimplenlg;

import java.io.BufferedReader;
import java.io.FileReader;
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

    final static String sentencePlan = "../../Punto4/sentence_plan";

    public static void main(String[] args) {

        Lexicon lexicon = Lexicon.getDefaultLexicon();
        NLGFactory nlgFactory = new NLGFactory(lexicon);
        Realiser realiser = new Realiser(lexicon);
        JSONParser parser = new JSONParser();

        try {
            BufferedReader b = new BufferedReader(new FileReader(sentencePlan));
            while (b.ready()) {
                String line = b.readLine();

                SPhraseSpec p = nlgFactory.createClause();

                JSONObject plan = (JSONObject) parser.parse(line);

                setVerb(plan, p);
                setSubj(plan, p, nlgFactory);
                setObj(plan, p, nlgFactory);
                setCterm(plan, p, nlgFactory);

                System.out.println(realiser.realiseSentence(p));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void setVerb(JSONObject plan, SPhraseSpec p) {

        p.setVerb((String) plan.get("VERB"));

    }

    private static void setSubj(JSONObject plan, SPhraseSpec p, NLGFactory nlgFactory) {

        JSONArray subjs = (JSONArray) plan.get("SUBJ");
        Iterator<String> it = subjs.iterator();
        JSONObject qsbj = (JSONObject) plan.get("QUANTSUBJ");
        int c = 0;
        NPPhraseSpec s1;
        String q = (qsbj != null) ? (String) qsbj.get(c + "") : null;
        if (q == null) {
            s1 = nlgFactory.createNounPhrase(it.next());
        } else {
            if (q.equals("exist")) {
                q = "a";
            }
            s1 = nlgFactory.createNounPhrase(q, it.next());
        }
        c++;
        if (subjs.size() > 1) {

            CoordinatedPhraseElement subj = null;
            while (it.hasNext()) {
                NPPhraseSpec s2;
                q = (qsbj != null) ? (String) qsbj.get(c + "") : null;
                if (q == null) {
                    s2 = nlgFactory.createNounPhrase(it.next());
                } else {
                    if (q.equals("exist")) {
                        q = "a";
                    }
                    s2 = nlgFactory.createNounPhrase(q, it.next());
                }
                if (subj != null) {
                    subj.addCoordinate(s2);
                } else {
                    subj = nlgFactory.createCoordinatedPhrase(s1, s2);
                }
                c++;
            }
            p.setSubject(subj);
        } else {

            p.setSubject(s1);
        }

    }

    private static void setObj(JSONObject plan, SPhraseSpec p, NLGFactory nlgFactory) {

        JSONArray objs = (JSONArray) plan.get("OBJ");
        if (objs != null) {
            Iterator<String> it = objs.iterator();
            JSONObject qobj = (JSONObject) plan.get("QUANTOBJ");
            int c = 0;

            NPPhraseSpec s1;
            String q = (qobj != null) ? (String) qobj.get(c + "") : null;
            if (q == null) {
                s1 = nlgFactory.createNounPhrase(it.next());
            } else {
                if (q.equals("exist")) {
                    q = "a";
                }
                s1 = nlgFactory.createNounPhrase(q, it.next());
            }
            c++;
            if (objs.size() > 1) {
                CoordinatedPhraseElement obj = null;
                while (it.hasNext()) {
                    NPPhraseSpec s2;
                    q = (qobj != null) ? (String) qobj.get(c + "") : null;
                    if (q == null) {
                        s2 = nlgFactory.createNounPhrase(it.next());
                    } else {
                        if (q.equals("exist")) {
                            q = "a";
                        }
                        s2 = nlgFactory.createNounPhrase(q, it.next());
                    }
                    if (obj != null) {
                        obj.addCoordinate(s2);
                    } else {
                        obj = nlgFactory.createCoordinatedPhrase(s1, s2);
                    }
                    c++;
                }
                p.setObject(obj);
            } else {
                p.setObject(s1);
            }
        }
    }

    private static void setCterm(JSONObject plan, SPhraseSpec p, NLGFactory nlgFactory) {

        if ((String) plan.get("CTERM") != null) {
            NPPhraseSpec cterm;
            String qct = (String) plan.get("QUANTCTERM");
            if (qct == null) {
                qct = "to";
            } else {
                qct = "to "+ qct;
            }
            cterm = nlgFactory.createNounPhrase(qct, (String) plan.get("CTERM"));
            
            p.addModifier(cterm);
        }
    }
}
