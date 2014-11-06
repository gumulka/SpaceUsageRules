#!/bin/bash

cd ..
doxygen Doxyfile
cp doc/latex/*.tex Documentation/
cp doc/latex/*.pdf Documentation/
cp doc/latex/*.eps Documentation/
cd Documentation
sed -n '/input{/p; /chapter{/p' refman.tex > doxyref.tex
make
