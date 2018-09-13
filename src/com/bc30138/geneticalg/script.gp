set term gif animate delay 10
set output "animate.gif"
set xrange [-20:-3.1]
unset key
do for [i=0:4] {
    plot (sin(2*x)/(x*x)) w li lw 1.5 lt rgb 'red', 'out/points.dat' index i u 1:2 w p pt 7 lt rgb 'blue'
}


