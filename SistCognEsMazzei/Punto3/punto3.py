import nltk
from nltk import load_parser
parser = load_parser('file:G2.fcfg', trace=0)

out_file = open("text_plan_nltk","w")

in_file = open("../frasi","r")
while 1:
    in_line = in_file.readline()
    if in_line == "":
        break
    print in_line
    sentence = in_line
    tokens = sentence.split()
    trees = parser.nbest_parse(tokens)
    for tree in trees:
        print tree.node['SEM']
        out_file.write(str(tree.node['SEM'])+"\n")
in_file.close()
out_file.close()
