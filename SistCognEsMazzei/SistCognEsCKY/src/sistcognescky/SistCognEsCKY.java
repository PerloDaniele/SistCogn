/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sistcognescky;

import java.util.ArrayList;
import java.util.List;
import sistcognescky.CFGrammar.Simbolo;

/**
 *
 * @author daniele
 */
public class SistCognEsCKY {

    static CFGrammar grammar = null;
    static final String fileCFG = "../CFG1";
    static final boolean DEBUG = false;
    

    public static void main(String[] args) {
        grammar = new CFGrammar(fileCFG);
        System.out.println("Grammatica Caricata");
        String frase = "Dante e un uomo visitano un inferno";
        System.out.println(frase + " -> " + CYK(frase));
        
    }

    private static Boolean CYK(String frase) {

        String[] words = frase.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].trim();
        }
        List<Simbolo>[][] table = new List[words.length][words.length+1];
        List<Simbolo> regola;
        for(int i=0;i<words.length;i++)
            for(int j=0;j<words.length+1;j++)
                table[i][j] = new ArrayList<>();

        for (int j = 1; j <= words.length; j++) {
            Simbolo s = grammar.terminali.get(words[j - 1]);
            if(s!=null) 
                table[j - 1][j] = s.from;
                
            for (int i = j - 2; i >= 0; i--) {
                for (int k = i + 1; k <= j - 1; k++) {
                    for (Simbolo s1 : table[i][k]) {
                        for (Simbolo s2 : table[k][j]) {
                            regola = new ArrayList<>();
                            regola.add(s1);
                            regola.add(s2);
                            if(DEBUG)System.out.println("regola - "+regola);
                            for (String nt : grammar.nonTerminali.keySet()) {
                                for (List produzione : grammar.nonTerminali.get(nt).produzioni) {
                                    if(DEBUG)System.out.println("produzione "+nt+" -> "+produzione);
                                    if (produzione.equals(regola) && !table[i][j].contains(grammar.nonTerminali.get(nt))) {
                                        table[i][j].add(grammar.nonTerminali.get(nt));
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
        
        for(int i=0;i<words.length;i++){
            for(int j=1;j<words.length+1;j++)
                System.out.print( ((i<j)? table[i][j]:"") + "\t" );
            System.out.println("");
        }
        
        return table[0][words.length].contains(grammar.init);
    }
}
