#!/bin/bash

convert -extract 260x160+2130+1140 -bordercolor red -border 4 $1 test.png

convert $1 test.png -geometry 268x168+2126+1136 -composite $2

convert $2 test.png -geometry 1072x672+200+1300 -composite $2

convert $2 -stroke red -draw "line 200,1300 2126,1136 line 1271,1598 2126,1302 line 1272,1300 2394,1136 line 1270,1970,2392,1302" $2

