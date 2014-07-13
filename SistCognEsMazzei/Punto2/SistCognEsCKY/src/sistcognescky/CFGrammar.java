/*
 * Classe che rappresenta una ContextFreeGrammar
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

	//mapping stringa->simbolo non terminale
    public HashMap<String, Simbolo> nonTerminali;
	//mapping stringa->simbolo terminale
    public HashMap<String, Simbolo> terminali;
	//simboli totali    
	public HashSet<Simbolo> simboli;
	// S    
	public Simbolo init;

    public CFGrammar(String fileCFG) {
		
		//lettura grammatica CF in forma normale di Chomsky
		//una regola di riscrittura per linea 
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

					//parte sinistra
                    nonTerm = new Simbolo(splits[0].trim());
                    if (nonTerminali.isEmpty()) {
                        init = nonTerm;
                    }
                    List<List<Simbolo>> produzioni = new ArrayList<>();
                    List<Simbolo> termini;

                    splits = line.split("\\|");
					
					//parte destra
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
					
					//inserisco nella lista dei non terminali
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

	//Classe che rappresenta il singolo simbolo
    class Simbolo {

		//stringa simbolo
        String t;
		//lita dei non terminali che producono il simbolo
        Set<Simbolo> from;
		//lista delle produzioni del simbolo
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
