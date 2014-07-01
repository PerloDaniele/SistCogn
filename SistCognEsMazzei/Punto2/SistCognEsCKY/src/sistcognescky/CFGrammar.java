/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sistcognescky;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author daniele
 */
public class CFGrammar {

    public HashMap<String, Simbolo> nonTerminali;
    public HashMap<String, Simbolo> terminali;
    public HashSet<Simbolo> simboli;
    public Simbolo init;

    public CFGrammar(String fileCFG) {

        try {
            BufferedReader b = new BufferedReader(new FileReader(fileCFG));
            String line;
            String[] splits, sprod;
            Simbolo nonTerm;
            nonTerminali = new HashMap<>();
            terminali = new HashMap<>();
            simboli= new HashSet();
            
            while (b.ready()) {
                line = b.readLine();
                if (!line.isEmpty()) {
                    splits = line.split(":=");
                    line = line.substring(line.indexOf(":=") + 2);
                    nonTerm = new Simbolo(splits[0].trim());
                    if (nonTerminali.isEmpty()) {
                        init = nonTerm;
                    }
                    List<List<Simbolo>> produzioni = new ArrayList<>();
                    List<Simbolo> termini;

                    splits = line.split("\\|");
                    for (int i = 0; i < splits.length; i++) {
                        termini = new ArrayList<>();
                        sprod = splits[i].trim().split(" ");
                        for (int j = 0; j < sprod.length; j++) {

                            Simbolo s = new Simbolo(sprod[j]);
                            s.addFrom(nonTerm);
                            termini.add(s);

                        }
                        for(Simbolo s:simboli){
                            for(Simbolo t:termini){
                                if(s.equals(t))
                                    t.from.addAll(s.from);
                            }
                        }
                        simboli.addAll(termini);
                        produzioni.add(termini);
                    }

                    if (nonTerminali.containsKey(nonTerm.t)) {
                        nonTerm = nonTerminali.get(nonTerm.t);
                    }
                    nonTerm.addProduzioni(produzioni);
                    
                    simboli.add(nonTerm);
                    nonTerminali.put(nonTerm.t, nonTerm);

                }
            }

            for (Simbolo s : simboli) {
                if (!nonTerminali.containsKey(s.t)) {
                    terminali.put(s.t, s);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Simbolo {

        String t;
        Set<Simbolo> from;
        List<List<Simbolo>> produzioni;
        
        Simbolo(String s) {
            from = new HashSet();
            produzioni = new ArrayList();
            t = s;
        }

        Simbolo() {
        }

        void addFrom(Simbolo s) {
            from.add(s);
        }
        
        @Override
        public String toString(){return t;}
        
        void addProduzione(List<Simbolo> l) {
            produzioni.add(l);
        }

        void addProduzioni(List<List<Simbolo>> l) {
            produzioni.addAll(l);
        }
        
        @Override
        public boolean equals(Object o){
            try{
                Simbolo s=(Simbolo)o;
                return s.t.equals(this.t);
                
            }catch(Exception e){return false;}
            
        }
    };

    
}
