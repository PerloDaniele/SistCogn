#
# Creazione di una PCFG estratta dal treebank Penn
#

import nltk
from nltk.corpus import treebank
from nltk.grammar import ContextFreeGrammar, Nonterminal, Production

#estrazione delle produzioni dal treebank
production_list = list(production for sent in treebank.parsed_sents()
                        for production in sent.productions())
tbank_productions = set(production_list)
tbank_grammar = ContextFreeGrammar(Nonterminal('S'), list(tbank_productions))

grammar_productions = tbank_grammar.productions()


lhsCount={}
prodCount={}
probs={}

#preparo le strutture dati: count(a->b)/count(a) con a non terminale
for production in production_list:
	if production.lhs() in lhsCount:
		lhsCount[production.lhs()] = lhsCount[production.lhs()] + 1
	else:
		lhsCount[production.lhs()] = 1
	if production in prodCount:
		prodCount[production] = prodCount[production] + 1
	else:
		prodCount[production] = 1

#calcolo count(a->b)/count(a) con a non terminale per ogni prduzione
for production in prodCount:

	probs[str(production)] = float(prodCount[production])/lhsCount[production.lhs()]


pcfg = []

#creo la pcfg, quindi inserendo le probabilita'
for p in probs:
	wf=str(p)
	#parse_pcfg non accetta tutta una serie di caratteri e non terminali composti da caratteri non alfanumerici
	#quindi sono necessarie un po' di replace
	wf=wf.replace(",","\",\"").replace("``","\"``\"").replace(".","\".\"").replace("=","--")
	wf=wf.replace(":","\":\"").replace("\'\'","\"\'\'\"").replace("#","\"#\"").replace("$","SS")
	wf=wf.replace("-LRB-","LRB-").replace("-NONE-","NONE-").replace("-RRB-","RRB-").replace("ADVP|PRT","ADV-PRT").strip()	
	if not wf.startswith('\"'):
		pcfg.append(wf+" ["+str('{0:.10f}'.format(probs[str(p)]))+"]")
	#print p

#creazione grammatica
grammar = nltk.parse_pcfg(pcfg)
viterbi_parser = nltk.ViterbiParser(grammar)
 
#frase di prova
sent = 'I can finally drink a beer now'
print sent
sent=sent.split()

#parsificazione
for tree in viterbi_parser.nbest_parse(sent,3):
	print tree


