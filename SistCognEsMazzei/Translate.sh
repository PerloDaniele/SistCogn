#!/bin/bash
python ./Punto3/punto3.py $1 | python ./Punto4/punto4.py | java -cp <Punto5/SistCognEsSimpleNLG> sistcognessimplenlg.SistCognEsSimpleNLG
