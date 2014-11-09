

all: main.pdf

doku: doxyref.tex

doxyref.tex:
	cd .. ; doxygen Doxyfile
	cp doc/class* .
	cp doc/dir* .
	cp doc/todo.tex .
	cd doc; sed -n '/input{/p; /chapter{/p' refman.tex > doxyref.tex
	cp doc/doxyref.tex .

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

graphics: LaufzeitGen90Q.png DataDrawerZoom.png

DataDrawerZoom.png:
	convert DataDrawer.png -crop 1000x700+1450+700 DataDrawerZoom.png

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
	rm -f ./class* ./dir* ./todo.tex ./doxyref.tex todo.tex
