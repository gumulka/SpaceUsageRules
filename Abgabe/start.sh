#!/bin/bash

# Testdatensatz
java -jar InMa.jar --data ../Testdatensatz/Data.txt --rules Rules.xml --outputDir computed --image images --overlap ../Testdatensatz/Overlap.txt

# Eigene Daten
java -jar InMa.jar --data ../Round2/Data.txt --rules Rules.xml --outputDir computed --image images
