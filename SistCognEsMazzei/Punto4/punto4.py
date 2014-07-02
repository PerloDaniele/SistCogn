
def remDup(seq):
    output = []
    for x in seq:
        if x not in output:
            output.append(x)
    return output



import re
import json

out_file = open("sentence_plan","w")

in_file = open("../Punto3/text_plan_nltk","r")

NounRE = '\w+\d*'
verbRE = '(.*\s)?(\w+)(\('+NounRE+','+NounRE+'(,('+NounRE+'))?\)).*'
verbIRE = '(.*\s)?(\w+)(\('+NounRE+'\)).*'
subjRE = '.*\(('+NounRE+').*'
objRE = '.*,('+NounRE+').*'
CTermRE = ',('+NounRE+')'
intransitive=0

while 1:
    sentence = in_file.readline()
    if sentence == "":
        break
    
    matchObj = re.match( verbRE , sentence, re.M|re.I)
    if matchObj is None:
        matchObj = re.match( verbIRE , sentence, re.M|re.I)
        intransitive=1
    if matchObj:
        print "\nFrase : ", matchObj.group()
        #print "verb : ", matchObj.group(2)
        verb=matchObj.group(2)
        subjs=[]
        objs=[]
        cterm=""

        if matchObj.group(3) != None:

            subjs_string = re.compile(verb+"\("+NounRE).findall(sentence)
            for s in subjs_string:
                matchS = re.match( subjRE , s , re.M|re.I)
                subjs.append(matchS.group(1))
            subjs=remDup(subjs)

            objs_string = re.compile(verb+"\("+NounRE+","+NounRE).findall(sentence)
            for o in objs_string:
                matchO = re.match( objRE , o, re.M|re.I)
                objs.append(matchO.group(1))
            objs=remDup(objs)

        if intransitive==0:   
            if matchObj.group(4) != None:
                matchCT = re.match( CTermRE , matchObj.group(4), re.M|re.I)
                cterm = matchCT.group(1)

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
        #print quantSbj
        #print subjs

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
        #print objs
        #print quantObj

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


        #splan={'VERB' : verb , 'SUBJ' : subjs , 'QUANTSUBJ' : quantSbj, 'OBJ' : objs , 'QUANTOBJ' : quantObj, 'CTERM' : cterm, 'QUANTCTERM' : quantCterm}
        splan={"VERB" : verb , "SUBJ" : subjs }
        if len(quantSbj)>0:
            splan["QUANTSUBJ"]=quantSbj
        if len(quantObj)>0:
            splan["QUANTOBJ"]=quantObj
        if len(objs)>0:
            splan["OBJ"]=objs
        if len(cterm)>0:
            splan["CTERM"]=cterm
        if len(quantCterm)>0:
            splan["QUANTCTERM"]=quantCterm

        splan=json.dumps(splan)
        print splan
        out_file.write(str(splan)+"\n")
        intransitive=0;
    else:
        print "No match!!"


in_file.close()
out_file.close()

