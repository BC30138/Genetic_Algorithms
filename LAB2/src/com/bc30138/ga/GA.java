package com.bc30138.ga;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

class point implements Comparable<point> // класс точка
{
    Vector<Double> x_value; // вектор значений в измерениях
    Double y_value; // значение функции в этой точке

    public int compareTo(point o) // оператор сравнения точек
    {
        point e = (point) o;

        int result = this.y_value.compareTo(e.y_value);

        if (result < 0) {
            return -1;
        } else {
            if (result == 0) {
                return 0;
            } else
                return 1;
        }
    }
};

public class GA { // главный класс программы
    String gnuplot_func; // второстепенная переменная для оформления графика
    String line_func; // второстепенная переменная для оформления графика
    Vector<point> points; // точки популяции

    int size; // размер популяции
    int dim_num; // количество измерений
    double alpha; // параметр для BLX
    double left, right; // левая и правая граница
    int step_count; // количество эпох
    double cros_prob, mut_prob; // вероятность кроссовера и мутации
    boolean plot_flag; // не строить график в случае dim_num > 2
    boolean dim_error_flag; // не считать, если dim_bum < 1
    int step_mut; // зависимость мутации от количества эпох

    private double func(Vector<Double> x) // вид функции
    {
        double f = 0;
        for (int it = 0; it < dim_num; ++it) {
            f += 5 * (it + 1) * x.get(it) * x.get(it);
        }
        return f;
    }

    public void plot() // функция построения графика
    {
        switch (dim_num) {
        case 1:
            try {
                FileWriter gnuscrWriter = new FileWriter("src/com/bc30138/ga/script.gp");
                PrintWriter gnuWriter = new PrintWriter(gnuscrWriter);
                gnuWriter.print("set term gif animate delay 15 font 'Helvetica,20' size 1024,500\n");
                gnuWriter.print("set xlabel 'X'\n");
                gnuWriter.print("set ylabel 'Y'\n");
                gnuWriter.print("set key spacing 1.5\n");
                gnuWriter.print("set output 'searchprocess.gif'\n");
                gnuWriter.printf("set xrange [%.1f:%.1f]\n", left, right);
                gnuWriter.print("set key left\n");
                gnuWriter.printf("do for [i=0:%d] {\n", step_count);
                gnuWriter.print("plot " + gnuplot_func + " w li lw 2 lt rgb 'red' ti '" + line_func
                        + "', 'out/points.dat' index i u 1:2 w p pt 7 ps 1.5 lc rgb 'blue' ti 'Extremum search'\n");
                gnuWriter.print("}\n");
                gnuWriter.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            break;
        case 2:
            try {
                FileWriter gnuscrWriter = new FileWriter("src/com/bc30138/ga/script.gp");
                PrintWriter gnuWriter = new PrintWriter(gnuscrWriter);
                gnuWriter.print("set term gif animate delay 15 font 'Helvetica,20' size 1024,500\n");
                // gnuWriter.print("set pm3d \n");
                // gnuWriter.print("set hidden3d \n");
                gnuWriter.print("set xlabel 'X'\n");
                gnuWriter.print("set ylabel 'Y'\n");
                gnuWriter.print("set zlabel 'Z'\n");
                gnuWriter.print("set ztics (0,150,300) \n");
                gnuWriter.print("set view 30,40 \n");
                gnuWriter.print("set key spacing 1.5\n");
                gnuWriter.print("set output 'searchprocess.gif'\n");
                gnuWriter.printf("set zrange [-0.5:400]\n");
                gnuWriter.printf("set xrange [%.1f:%.1f]\n", left, right);
                gnuWriter.printf("set yrange [%.1f:%.1f]\n", left, right);
                gnuWriter.print("set key left\n");
                gnuWriter.printf("do for [i=0:%d] {\n", step_count);
                gnuWriter.print("splot" + gnuplot_func + "w pm3d ti '" + line_func
                        + "', 'out/points.dat' index i u 1:2:3 w p pt 7 ps 1.5 lc rgb 'red' ti 'Extremum search'\n");
                gnuWriter.print("}\n");
                gnuWriter.print("}\n");

                gnuWriter.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            break;
        }

        final Runtime run = Runtime.getRuntime();
        try {
            String fpath = "gnuplot src/com/bc30138/ga/script.gp";
            Process p = run.exec(fpath);
            int result = p.waitFor();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public GA(int size, int dim_num, int step_count, double left, double right, double alpha, double cros_prob,
            double mut_prob, int step_mut) // конструктор главного класса
    {
        this.size = size;
        this.step_count = step_count;
        this.left = left;
        this.right = right;
        this.alpha = alpha;
        this.cros_prob = cros_prob;
        this.mut_prob = mut_prob;
        this.step_mut = step_mut;
        this.points = new Vector<point>();

        switch (dim_num) {
        case 1:
            this.dim_error_flag = false;
            this.plot_flag = true;
            this.dim_num = dim_num;
            this.gnuplot_func = "(5*x*x)";
            this.line_func = "5x^2";
            break;
        case 2:
            this.dim_error_flag = false;
            this.plot_flag = true;
            this.dim_num = dim_num;
            this.gnuplot_func = "(5*x*x + 10*y*y)";
            this.line_func = "5x^2 + 10y^2";
            break;
        default:
            if (dim_num < 1) {
                this.dim_error_flag = true;
                System.out.println("Error: bad dimension parameter.");
            } else {
                this.dim_error_flag = false;
                this.dim_num = dim_num;
                plot_flag = false;
            }
            break;
        }

    }

    public void start() {
        if (!dim_error_flag) {
            init_first_gen();
            process();
            System.out.printf("max in (%.3f", Math.max(0, points.get(0).x_value.get(0)));
            for (int jt = 1; jt < dim_num; ++jt)
                System.out.printf(",%.3f", Math.max(0, points.get(0).x_value.get(jt)));
            System.out.printf(",%.3f)\n", Math.max(0, points.get(0).y_value));
            if (plot_flag)
                plot();
        }
    }

    public void init_first_gen() {
        for (int it = 0; it < size; ++it) // инициализация начальной популяции
        {
            point temp = new point();
            temp = randomizer();
            points.addElement(temp);
        }
    }

    private point randomizer()// функция создания рандомной точки
    {
        point new_point = new point();
        Random random = new Random();
        new_point.x_value = new Vector<Double>();
        for (int it = 0; it < dim_num; ++it) {
            double randomValue = left + (right - left) * random.nextDouble();
            new_point.x_value.addElement(randomValue);
        }
        new_point.y_value = func(new_point.x_value);
        return new_point;
    }

    public void process() {
        // int it_count = 0;
        // int it_flag = 0;
        try {
            FileWriter fileWriter = new FileWriter("out/points.dat");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            Collections.sort(points);
            to_file(printWriter);// вывод точек в файл
            for (int it = 0; it < step_count; ++it) // основной цикл (итерация по эпохам)
            {
                crossing(); // вызов оператора кроссинговера
                mutation(it, step_mut); // вызов оператора мутации
                screenings(size); // удаление точек
                to_file(printWriter);
            }
            printWriter.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void to_file(PrintWriter printWriter) // вывод в файл
    {
        for (int it = 0; it < points.size(); ++it) {
            for (int jt = 0; jt < dim_num; ++jt)
                printWriter.printf("%.4f\t", points.get(it).x_value.get(jt));
            printWriter.printf("%.4f \n", points.get(it).y_value);
        }
        printWriter.print("\n\n");
    }

    private void crossing() // оператор кроссинговера (BLX-alpha)
    {
        int tournament = (int) Math.round(points.size() / 2);
        boolean used_flag[][] = new boolean[tournament][tournament];
        for (int it = 0; it < tournament; ++it) {
            if (new Random().nextDouble() <= cros_prob) { // скрещивание происходит с некоторой вероятностью

                int jt = ThreadLocalRandom.current().nextInt(0, tournament); // выбор родителя с учетом совпадений
                while ((it == jt) || used_flag[it][jt])
                    jt = ThreadLocalRandom.current().nextInt(0, tournament);
                used_flag[it][jt] = true;

                point new_point = new point();
                new_point.x_value = new Vector<Double>();

                Random random = new Random(); // BLX-alpha
                for (int x_it = 0; x_it < dim_num; ++x_it) {
                    double blx_min, blx_max, blx_left, blx_right;
                    if (points.get(it).x_value.get(x_it) < points.get(jt).x_value.get(x_it)) {
                        blx_min = points.get(it).x_value.get(x_it);
                        blx_max = points.get(jt).x_value.get(x_it);
                    } else {
                        blx_min = points.get(jt).x_value.get(x_it);
                        blx_max = points.get(it).x_value.get(x_it);
                    }
                    blx_left = blx_min - (blx_max - blx_min) * alpha;
                    blx_right = blx_max + (blx_max - blx_min) * alpha;
                    new_point.x_value.addElement(blx_left + (blx_right - blx_left) * random.nextDouble());
                }
                new_point.y_value = func(new_point.x_value);
                points.addElement(new_point);
            }
        }
    }

    private void mutation(int step_it, int step_mut) // оператор мутации (неравномерный)
    {
        for (int it = 0; it < points.size(); ++it) {
            if (new Random().nextDouble() <= mut_prob) {// мутация происходит с некоторой вероятностью
                Random random = new Random();
                int k = ThreadLocalRandom.current().nextInt(0, dim_num);
                point new_point = new point();
                new_point = points.get(it);
                if (random.nextBoolean()) {
                    new_point.x_value.set(k, points.get(it).x_value.get(k) - (points.get(it).x_value.get(k) - left)
                            * (1 - Math.pow(Math.random(), Math.pow(1 - step_it / step_count, step_mut))));
                    new_point.y_value = func(new_point.x_value);
                    points.set(it, new_point);
                } else {
                    new_point.x_value.set(k, points.get(it).x_value.get(k) + (right - points.get(it).x_value.get(k))
                            * (1 - Math.pow(Math.random(), Math.pow(1 - step_it / step_count, step_mut))));
                    new_point.y_value = func(new_point.x_value);
                    points.set(it, new_point);
                }
            }
        }
    }

    private void screenings(double size) // удаление точек до тех пор пока не останется
                                         // изначальное количество точек
    {
        // точки сортируются по неубыванию, после чего первые элементы удаляются
        Collections.sort(points);
        while (points.size() > size) {
            points.remove(points.size() - 1);
        }
    }

    public static void main(String[] args) // работа программы
    {
        GA test = new GA(50, // размер популяции
                2, // количество измерений
                40, // количество итераций
                -5.12, 5.12, // границы
                0.5, // alpha для BLX-alpha
                0.5, // вероятность кроссинговера
                0.001, // вероятность мутации
                2 // зависимость мутации от числа эпох
        );
        test.start();
    }
}
