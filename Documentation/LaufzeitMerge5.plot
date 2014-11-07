set terminal png size 2000,500 enhanced font "Helvetica,20"
set output 'LaufzeitMerge5Q.png'

set multiplot;                          # get into multiplot mode
set size 0.33,1;
set yrange [165500:166200]

set origin 0.0,0;
set xrange [0:600]
plot 'tmp.txt' using 6:2 title 'popsize'

set origin 0.33,0;
set xrange [0:600]
plot 'tmp.txt' using 8:2 title 'withouts'

set origin 0.66,0;
set xrange [0:5]
plot 'tmp.txt' using 10:2 title 'mutations'

unset multiplot



set output 'LaufzeitMerge5T.png'
set multiplot
set size 0.33,1;
set   autoscale                        # scale axes automatically
set ydata time
set timefmt '%H:%M:%S'

set origin 0.0,0.0;
set xrange [0:600]
plot 'tmp.txt' using 6:4 title 'popsize'

set origin 0.33,0.0;
set xrange [0:600]
plot 'tmp.txt' using 8:4 title 'withouts'

set origin 0.66,0.0;
set xrange [0:5]
plot 'tmp.txt' using 10:4 title 'mutations'

unset multiplot                         # exit multiplot mode
