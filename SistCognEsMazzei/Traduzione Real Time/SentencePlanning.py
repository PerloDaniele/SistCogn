
#produzione di un sentence-plan
#partendo dal text-plan costruito con l'ausilio di nltk, viene generata
#una struttura gerarchica contenente gli elementi del discorso descritti in modo
#tale da poter essere utilizzata come base per la generazione automatica di frasi.
#

import sys
import re
import json

INPUT = sys.stdin

def remDup(seq):
    output = []
    for x in seq:
        if x not in output:
            output.append(x)
    return output

def sentencePlan(sentence):

	#regular expression per la cattura degli elementi sintattici della frase
	#descritti in forma semantica
    NounRE = '\w+\d*'
    verbRE = '(.*\s)?(\w+)(\('+NounRE+','+NounRE+'(,('+NounRE+'))?\)).*'
    verbIRE = '(.*\s)?(\w+)(\('+NounRE+'\)).*'
    subjRE = '.*\(('+NounRE+').*'
    objRE = '.*,('+NounRE+').*'
    CTermRE = ',('+NounRE+')'
    intransitive=0
    splan="Frase non gestita"

	#verbo transitivo
    matchObj = re.match( verbRE , sentence, re.M|re.I)
    if matchObj is None:
		#verbo intransitivo
        matchObj = re.match( verbIRE , sentence, re.M|re.I)
        intransitive=1
    if matchObj:
        verb=matchObj.group(2)
        subjs=[]
        objs=[]
        cterm=""

        if matchObj.group(3) != None:
			#recupero di tutti i soggetti
            subjs_string = re.compile(verb+"\("+NounRE).findall(sentence)
            for s in subjs_string:
                matchS = re.match( subjRE , s , re.M|re.I)
                subjs.append(matchS.group(1))
            subjs=remDup(subjs)
			#recupero di tutti gli oggetti
            objs_string = re.compile(verb+"\("+NounRE+","+NounRE).findall(sentence)
            for o in objs_string:
                matchO = re.match( objRE , o, re.M|re.I)
                objs.append(matchO.group(1))
            objs=remDup(objs)

		#recupero del complemento di termine ove presente
        if intransitive==0:   
            if matchObj.group(4) != None:
                matchCT = re.match( CTermRE , matchObj.group(4), re.M|re.I)
                cterm = matchCT.group(1)

		#definizione dei quantificatori per i soggetti per le variabili libere e traduzione nei "nomi comuni" che le descrivono
        quantSbj={}
        for i in range(0,len(subjs)):
            while len(subjs[i])<=2:
                newsbj = re.match('.*\(('+NounRE+')\(('+subjs[i]+')\).*|.*\s'+subjs[i]+'\(('+NounRE+')\).*' , sentence, re.M|re.I)
                if not quantSbj.has_key(i):
                    exist = re.match('.*exists\s'+subjs[i]+'.*' , sentence, re.M|re.I)
                    if exist is None:
                        quantSbj[i] = "all"
                    else:
                        quantSbj[i] = "exist"
                if newsbj.group(3) is None:
                    subjs[i] = newsbj.group(1)
                else:
                    subjs[i] = newsbj.group(3)

		#definizione dei quantificatori per gli oggetti per le variabili libere e traduzione nei "nomi comuni" che le descrivono

        quantObj={}
        for i in range(0,len(objs)):
            while len(objs[i])<=2:
                newobj = re.match( '.*\(('+NounRE+')\(('+objs[i]+')\).*|.*\s'+objs[i]+'\(('+NounRE+')\).*' , sentence, re.M|re.I)
                if not quantObj.has_key(i):
                    exist = re.match('.*exists\s'+objs[i]+'.*' , sentence, re.M|re.I)
                    if exist is None:
                        quantObj[i] = "all"
                    else:
                        quantObj[i] = "exist"
                if newobj.group(3) is None:
                    
                    objs[i] = newobj.group(1)
                else:
                    objs[i] = newobj.group(3)
        
		#definizione del quantificatore per il complemento di termine per eventuale variabile libera e traduzione nel "nome comune" che la descrive
        quantCterm=""
        while (len(cterm)>0 and len(cterm)<=2):
            newcterm = re.match( '.*\(('+NounRE+')\(('+cterm+')\).*|.*\s'+cterm+'\(('+NounRE+')\).*' , sentence, re.M|re.I)
            if quantCterm!='':
                exist = re.match('.*exists\s'+cterm+'.*' , sentence, re.M|re.I)
                if exist is None:
                    quantCterm[0] = "all"
                else:
                    quantCterm[0] = "exist"
            if newcterm.group(3) is None:   
                cterm = newobj.group(1)
            else:
                cterm = newobj.group(3)

		#creazione json contenente il sentence plan
		#soggetti
        for i in range(0,len(subjs)):
            subjSpec={}
            subjSpec['NOUN']=subjs[i]
            if i in quantSbj:
                subjSpec['QUANT']=quantSbj[i]
                if quantSbj[i]=="all":
                    subjSpec['NUM']="pl"
                else:
                    subjSpec['NUM']="sg"
            else:
                subjSpec['NUM']="sg"
            subjs[i]=subjSpec

		#oggetti
        for i in range(0,len(objs)):
            objSpec={}
            objSpec['NOUN']=objs[i]
            if i in quantObj:
                objSpec['QUANT']=quantObj[i]
                if quantObj[i]=="all":
                    objSpec['NUM']="pl"
                else:
                    objSpec['NUM']="sg"
            else:
                objSpec['NUM']="sg"
            objs[i]=objSpec
    
		#complemento di termine
        if len(cterm)>0:
            cTermSpec={}
            cTermSpec['NOUN']=cterm
            if len(quantCterm)>0:
                cTermSpec['QUANT']=quantCterm[i]
                if quantCterm=="all":
                    cTermSpec['NUM']="pl"
                else:
                    cTermSpec['NUM']="sg"
            else:
                cTermSpec['NUM']="sg"
            cterm=cTermSpec

        splan={"VERB" : verb , "SUBJ" : subjs }
        if len(objs)>0:
            splan["OBJ"]=objs
        if len(cterm)>0:
            splan["CTERM"]=cterm

        splan=json.dumps(splan)

    else:
        print "No match!!"
    return splan



def main():
    fromStdin = 0
    if len(sys.argv)>1: 
        print sentencePlan(sys.argv[1])
    else: 
        for line in INPUT: 
            fromStdin=1
            print sentencePlan(line)
            break
        
        if fromStdin==0:
            out_file = open("sentence_plan","w")

            in_file = open("../Punto3/text_plan_nltk","r")

            while 1:
                sentence = in_file.readline()
                if sentence == "":
                    break
        
                splan = sentencePlan(sentence)
                print splan
                out_file.write(str(splan)+"\n")

            in_file.close()
            out_file.close()


if __name__ == '__main__':
    main()
