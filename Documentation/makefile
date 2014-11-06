

all: main.pdf

main.pdf: clean main.tex
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


show:
	evince main.pdf

clean:
	rm -f *.ps *.dvi *.aux *.toc *.idx *.ind *.ilg *.log *.out *.brf *.blg *.bbl main.pdf
