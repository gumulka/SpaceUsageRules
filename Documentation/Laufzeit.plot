set terminal png size 2000,500 enhanced font "Helvetica,20"
set output 'LaufzeitGen90Q.png'

set multiplot;                          # get into multiplot mode
set size 0.25,1;
set   autoscale                        # scale axes automatically

set origin 0.0,0;
set xrange [0:600]
plot 'LaufzeitGen90.txt' using 6:2 title 'popsize'

set origin 0.25,0;
set xrange [0:600]
plot 'LaufzeitGen90.txt' using 8:2 title 'withouts'

set origin 0.5,0;
set xrange [0:7]
plot 'LaufzeitGen90.txt' using 10:2 title 'mutations'

set origin 0.75,0;
set xrange [0:7]
plot 'LaufzeitGen90.txt' using 12:2 title 'merges'
unset multiplot


set output 'LaufzeitGen90T.png'
set multiplot
set size 0.25,1;
set ydata time
set timefmt '%H:%M:%S'

set origin 0.0,0.0;
set xrange [0:600]
plot 'LaufzeitGen90.txt' using 6:4 title 'popsize'

set origin 0.25,0.0;
set xrange [0:600]
plot 'LaufzeitGen90.txt' using 8:4 title 'withouts'

set origin 0.5,0.0;
set xrange [0:7]
plot 'LaufzeitGen90.txt' using 10:4 title 'mutations'

set origin 0.75,0.0;
set xrange [0:7]
plot 'LaufzeitGen90.txt' using 12:4 title 'merges'

unset multiplot                         # exit multiplot mode
