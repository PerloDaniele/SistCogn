/*
 * Classe che implementa l'algoritmo CKY per una grammatica CF in forma normale di chomsky 
 *
 */
package sistcognescky;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sistcognescky.CFGrammar.Simbolo;

/**
 *
 * @author daniele
 */
public class SistCognEsCKY {

    static CFGrammar grammar = null;
	//grammatica da caricare
    static final String fileCFG = "../../Punto1/CFG1";
    static final boolean DEBUG = false;
	//frase da provare
    static final String frase="Dante e una donna lasciano un dono a Virgilio";

    public static void main(String[] args) {
		//caricamento grammatica
        grammar = new CFGrammar(fileCFG);
        System.out.println("Grammatica Caricata");
		//esecuzione CKY
        System.out.println(frase + " -> " + CKY(frase));
        
    }

    private static Boolean CKY(String frase) {

        String[] words = frase.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].trim();
        }
		//inizializzo la struttura
        Set<Simbolo>[][] table = new Set[words.length][words.length+1];
        List<Simbolo> regola;
        for(int i=0;i<words.length;i++)
            for(int j=0;j<words.length+1;j++)
                table[i][j] = new HashSet<>();

		//Ciclo sulle colonne
        for (int j = 1; j <= words.length; j++) {
			//parto dal fondo e metto la produzione che mi genera il non terminale
            Simbolo s = grammar.terminali.get(words[j - 1]);
            if(s!=null) 
                table[j - 1][j] = s.from;
            //ciclo sulle righe
            for (int i = j - 2; i >= 0; i--) {
				//ciclo sulle colonne già passate
                for (int k = i + 1; k <= j - 1; k++) {
					//cerco le produzioni equivalenti ad ogni coppia di simboli in [i][k] e [k][j] e la aggiungo a [i][j]
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
        //Stampa
        for(int i=0;i<words.length;i++){
            for(int j=1;j<words.length+1;j++)
                System.out.print( ((i<j)? table[i][j]:"") + "\t" );
            System.out.println("");
        }
        //successo se ho almeno un simbolo S nell'angolo in alto a destra
        return table[0][words.length].contains(grammar.init);
    }
}
