#!/bin/bash

for D in 1 5 10 15
do

for G in 4 16 64
do

awk '$14 == '$G' && $20 == '$D' {print $0}' LaufzeitNeu.txt > tmp.txt
INPUT=tmp.txt

gnuplot <<PLOT
set terminal png size 2000,500 enhanced font "Helvetica,20"
set output 'images/Laufzeit-$D-$G-Q.png'

set multiplot;                          # get into multiplot mode
set size 0.25,1;
set yrange [130000:210000]

set origin 0.0,0;
set xrange [0:110]
plot '$INPUT' using 6:2 title 'popsize'

set origin 0.25,0;
set xrange [0:110]
plot '$INPUT' using 8:2 title 'withouts'

set origin 0.5,0;
set xrange [0:8]
plot '$INPUT' using 10:2 title 'mutations'

set origin 0.75,0;
set xrange [0:8]
plot '$INPUT' using 12:2 title 'merges'
unset multiplot
PLOT

gnuplot <<PLOT
set terminal png size 2000,500 enhanced font "Helvetica,20"
set output 'images/Laufzeit-$D-$G-T.png'
set autoscale                        # scale axes automatically
set multiplot
set size 0.25,1;
set ydata time
set yrange [0:660]
set timefmt '%H:%M:%S'

set origin 0.0,0.0;
set xrange [0:110]
plot '$INPUT' using 6:4 title 'popsize'

set origin 0.25,0.0;
set xrange [0:110]
plot '$INPUT' using 8:4 title 'withouts'

set origin 0.5,0.0;
set xrange [0:8]
plot '$INPUT' using 10:4 title 'mutations'

set origin 0.75,0.0;
set xrange [0:8]
plot '$INPUT' using 12:4 title 'merges'

unset multiplot                         # exit multiplot mode
PLOT

##    merges auf 5
awk '$12 ~ 5 {print $0}' tmp.txt > tmp2.txt
INPUT=tmp2.txt
gnuplot <<PLOT
set terminal png size 2000,500 enhanced font "Helvetica,20"
set output 'images/Laufzeit-$D-$G-Q-5.png'

set multiplot;                          # get into multiplot mode
set size 0.33,1;
set yrange [130000:210000]

set origin 0.0,0;
set xrange [0:110]
plot '$INPUT' using 6:2 title 'popsize'

set origin 0.33,0;
set xrange [0:110]
plot '$INPUT' using 8:2 title 'withouts'

set origin 0.66,0;
set xrange [0:8]
plot '$INPUT' using 10:2 title 'mutations'
unset multiplot
PLOT

gnuplot <<PLOT
set terminal png size 2000,500 enhanced font "Helvetica,20"
set output 'images/Laufzeit-$D-$G-T-5.png'
set autoscale                        # scale axes automatically
set multiplot
set size 0.33,1;
set ydata time
set yrange [0:660]
set timefmt '%H:%M:%S'

set origin 0.0,0.0;
set xrange [0:110]
plot '$INPUT' using 6:4 title 'popsize'

set origin 0.33,0.0;
set xrange [0:110]
plot '$INPUT' using 8:4 title 'withouts'

set origin 0.66,0.0;
set xrange [0:8]
plot '$INPUT' using 10:4 title 'mutations'

unset multiplot                         # exit multiplot mode
PLOT

done
done
rm tmp.txt tmp2.txt
