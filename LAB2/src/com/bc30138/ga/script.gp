set term gif animate delay 15 font 'Helvetica,18' size 1024,600
set xlabel 'X'
set ylabel 'Y'
set zlabel 'Z'
set ztics (0,150,300) 
set view 40,40 
set key spacing 1.5
set output 'searchprocess.gif'
set zrange [-0.5:400]
set xrange [-5.1:5.1]
set yrange [-5.1:5.1]
set key left
do for [i=0:40] {
splot(5*x*x + 10*y*y)w pm3d ti '5x^2 + 10y^2', 'out/points.dat' index i u 1:2:3 w p pt 7 ps 1.5 lc rgb 'red' ti 'Extremum search'
}
}
