

all: main.pdf

doku: doxyref.tex

doxyref.tex:
	cd .. ; doxygen Doxyfile
	cp doc/class* .
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

graphics: images/Laufzeit_10_16_Q.png images/DataDrawerZoom.png

images/DataDrawerZoom.png:
	convert images/DataDrawer.png -crop 1000x700+1450+700 images/DataDrawerZoom.png

images/Laufzeit_10_16_Q.png:
	./Laufzeit.plot
#	gnuplot -e "load 'LaufzeitMerge5.plot'"

show:
	evince main.pdf &

clean:
	rm -f *.ps *.dvi *.aux *.toc *.idx *.ind *.ilg *.log *.out *.brf *.blg *.bbl main.pdf

clean_all: clean
	rm -f -r doc
	rm -f ./images/Laufzeit*.png
	rm -f ./class* ./dir* ./doxyref.tex
