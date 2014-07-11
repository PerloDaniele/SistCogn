import nltk
from nltk.corpus import treebank
from nltk.grammar import ContextFreeGrammar, Nonterminal, Production

production_list = list(production for sent in treebank.parsed_sents()
                        for production in sent.productions())
tbank_productions = set(production_list)
tbank_grammar = ContextFreeGrammar(Nonterminal('S'), list(tbank_productions))

grammar_productions = tbank_grammar.productions()


lhsCount={}
prodCount={}
probs={}

for production in production_list:
	if production.lhs() in lhsCount:
		lhsCount[production.lhs()] = lhsCount[production.lhs()] + 1
	else:
		lhsCount[production.lhs()] = 1
	if production in prodCount:
		prodCount[production] = prodCount[production] + 1
	else:
		prodCount[production] = 1

for production in prodCount:

	probs[str(production)] = float(prodCount[production])/lhsCount[production.lhs()]


pcfg = []

for p in probs:
	wf=str(p)
	wf=wf.replace(",","\",\"").replace("``","\"``\"").replace(".","\".\"").replace("=","--")
	wf=wf.replace(":","\":\"").replace("\'\'","\"\'\'\"").replace("#","\"#\"").replace("$","SS")
	wf=wf.replace("-LRB-","LRB-").replace("-NONE-","NONE-").replace("-RRB-","RRB-").replace("ADVP|PRT","ADV-PRT").strip()	
	if not wf.startswith('\"'):
		pcfg.append(wf+" ["+str('{0:.10f}'.format(probs[str(p)]))+"]")
	#print p


grammar = nltk.parse_pcfg(pcfg)
viterbi_parser = nltk.ViterbiParser(grammar)
 
sent = 'I can finally drink a beer now'.split()
print sent

for tree in viterbi_parser.nbest_parse(sent,3):
	print tree


