#!/bin/bash

./LinguisticRealization.sh "$(python TextPlanning.py "$1" | python SentencePlanning.py)"
