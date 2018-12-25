set term gif animate delay 15 font 'Helvetica,18' size 1024,600
set xlabel 'X'
set ylabel 'Y'
set key spacing 1.5
set output 'searchprocess.gif'
set key right
do for [i=0:185] {
plot '/Users/bc30138/Documents/CODE/JAVA/GA/LAB3/./data/best.dat' u 1:2 w lines lw 2 lt rgb 'red' ti 'Opt result', 'data/output.dat' index i u 1:2 w lines lw 1 lt rgb 'blue' ti 'Opt search'
}
