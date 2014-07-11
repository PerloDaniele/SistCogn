
#produzione di text-plan
#utilizzo della grammatica context-free arricchita di semantica
#per costruire lambda-espressioni che caratterizzano la semantica 
#delle frasi in input che sono producibili dalla grammatica G2

import sys
import nltk
from nltk import load_parser

#parsificazione della sentence tramite il nltk.parser
#il quale crea i possibili alberi semantici derivabili
#dalla grammatica
def textPlan(sentence,parser):
    tokens = sentence.split()
    trees = parser.nbest_parse(tokens)
    return trees

INPUT = sys.stdin
#load della grammatica
parser = load_parser('file:G2.fcfg', trace=0)

#sentence come argomento
if len(sys.argv)>1: 
    trees =  textPlan(sys.argv[1],parser)
    for tree in trees:
        print tree.node['SEM']
else: 
    
    #sentence in sys.stdin
    for line in INPUT: 
        trees =  textPlan(line,parser)
        for tree in trees:
            print tree.node['SEM']
        break
    else:
        #lista di sentence in input
        #frasi di esempio da cui è stata derivata la grammatica
        out_file = open("text_plan_nltk","w")
        in_file = open("../frasi","r")
        while 1:
            #una sentence per riga
            in_line = in_file.readline()
            if in_line == "":
                break
            print in_line
            sentence = in_line
            #parsificazione
            trees = textPlan(sentence,parser)
            for tree in trees:
                #stampe
                print tree.node['SEM']
                out_file.write(str(tree.node['SEM'])+"\n")
        in_file.close()
        out_file.close()
