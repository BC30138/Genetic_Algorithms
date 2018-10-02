set term gif animate delay 15 font 'Helvetica,12'
set xlabel 'X'
set ylabel 'Y'
set key spacing 1.5
set output 'searchprocess.gif'
set xrange [-20.0:-3.1]
set key left
do for [i=0:50] {
plot(sin(2*x)/(x*x))w li lw 1.5 lt rgb 'red' ti 'sin(2x)/(x^2)', 'out/points.dat' index i u 1:2 w p pt 7 ps 1 lc rgb 'blue' ti 'Extremum search'
}
