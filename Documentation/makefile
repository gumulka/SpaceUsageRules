

all: main.pdf

doku: doc/doxyref.tex

doc/doxyref.tex:
	cd .. ; doxygen Doxyfile
	cd doc; sed -i 's/classde/doc\/classde/' *.tex ; sed -n '/input{/p; /chapter{/p' refman.tex > doxyref.tex

main.pdf: clean main.tex doku graphics
	pdflatex main
	makeindex main.idx
	pdflatex main
	latex_count=8 ; \
	while egrep -s 'Rerun (LaTeX|to get cross-references right)' main.log && [ $$latex_count -gt 0 ] ;\
	    do \
	      echo "Rerunning latex...." ;\
	      pdflatex main ;\
	      latex_count=`expr $$latex_count - 1` ;\
	    done
	makeindex main.idx
	pdflatex main

graphics: LaufzeitGen90Q.png

LaufzeitGen90Q.png:
	gnuplot -e "load 'Laufzeit.plot'"
	awk '$$12 ~ 5 && $$10 < 6 {print $$0}' LaufzeitGen90.txt > tmp.txt
	gnuplot -e "load 'LaufzeitMerge5.plot'"
	rm tmp.txt

show:
	evince main.pdf

clean:
	rm -f *.ps *.dvi *.aux *.toc *.idx *.ind *.ilg *.log *.out *.brf *.blg *.bbl main.pdf

clean_all: clean
	rm -f -r doc
	rm -f ./Laufzeit*.png
