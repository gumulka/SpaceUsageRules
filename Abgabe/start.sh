#!/bin/bash

# Testdatensatz
java -jar InMa.jar -d ../Testdatensatz/Data.txt -r Rules.xml -o computed -i images -u ../Testdatensatz/Overlap.txt

# Eigene Daten
java -jar InMa.jar -d ../Round2/Data.txt -r Rules.xml -o computed -i images
