package com.bc30138.geneticalg;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;
import javafx.util.Pair; 
import java.math.BigDecimal;

class point implements Comparable<point> //Так как язык объектно-ориентированный - все делается средствами классов, это класс объекта точки
{
    Vector<Integer> binary_x; //двоичное значение x
    Double x_value; //десятичное значение x
    Double y_value; //двоичное значение y

    public int compareTo(point o) //оператор сравнения точек
	{
        point e = (point) o;

        int result = this.y_value.compareTo(e.y_value);
    
        if (result < 0)
        {
            return -1;
        }
        else 
        {
            if (result == 0)
            {
                return 0;
            } 
            else return 1;
        }   
	}
};

public class GA { //главный класс программы
    int bit_number; //количество бит в двоичном представлении
    String gnuplot_func; //второстепенная переменная для оформления графика
    String line_func; //второстепенная переменная для оформления графика
    Vector<point> points = new Vector<point>(); //точки популяции
    Vector<Integer> wheel_temp_p = new Vector<Integer>(); //второстепенная переменная для оптимизации оператора кроссинговера
    Vector<Integer> wheel_only_p = new Vector<Integer>(); //второстепенная переменная для оптимизации оператора кроссинговера
    double summ; //сумма значений всех точек
    int step_count_; //количество итераций
    double left_, right_; //левая и правая граница

    private point init_point(Vector<Integer> bin) //функция инициализации точки
    {
        point new_point = new point();
        new_point.binary_x = bin;
        new_point.x_value = bin_to_num(bin);
        new_point.y_value = func(new_point.x_value);
        return new_point;
    }

    private double func(double x) //вид функции
    { 
        gnuplot_func = "(sin(2*x)/(x*x))";
        line_func = "sin(2x)/(x^2)";
        return Math.sin(2 * x) / Math.pow(x, 2); 
    }

    private double bin_to_num(Vector<Integer> binary_num)//функция перевода двоичного числа в десятичное
    {
        double number = 0;

        for (int it = 0; it < bit_number; ++it)
        {
            number += binary_num.get(it) * Math.pow(2, it);
        }

        return left_ + number * (right_ - left_) / (Math.pow(2, bit_number) - 1);
    }
    
    private point randomizer()//функция создания рандомной точки
    {
        point new_point = new point();
        Random random_ = new Random();
        new_point.binary_x = new Vector<Integer>(bit_number);
        for(int i = 0; i < bit_number; ++i)
        {
            int val = random_.nextBoolean() ? 1 : 0;
            new_point.binary_x.addElement(val);
        }
        new_point.x_value = bin_to_num(new_point.binary_x);
        new_point.y_value = func(new_point.x_value);
        return new_point;
    } 

    public void plot() //функция построения графика
    {
        try {
            FileWriter gnuscrWriter = new FileWriter("src/com/bc30138/geneticalg/script.gp");
            PrintWriter gnuWriter = new PrintWriter(gnuscrWriter);
            gnuWriter.print("set term gif animate delay 15 font 'Helvetica,12'\n");
            gnuWriter.print("set xlabel 'X'\n");
            gnuWriter.print("set ylabel 'Y'\n");
            gnuWriter.print("set key spacing 1.5\n");
            gnuWriter.print("set output 'searchprocess.gif'\n");
            gnuWriter.printf("set xrange [%.1f:%.1f]\n", left_, right_); 
            gnuWriter.print("set key left\n");
            gnuWriter.printf("do for [i=0:%d] {\n",step_count_);
            gnuWriter.print("plot" + gnuplot_func + "w li lw 1.5 lt rgb 'red' ti '" + line_func + 
            "', 'out/points.dat' index i u 1:2 w p pt 7 ps 1 lc rgb 'blue' ti 'Extremum search'\n");
            gnuWriter.print("}\n");
            gnuWriter.close();
        }   catch (final IOException e) {
            e.printStackTrace();
        }
        
        final Runtime run = Runtime.getRuntime();
        try {
            String fpath = "gnuplot src/com/bc30138/geneticalg/script.gp";
            Process p = run.exec(fpath);
            int result = p.waitFor();
         } catch (final IOException e) {
            e.printStackTrace();
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
    }

    public void to_file(PrintWriter printWriter) //вывод в файл
    {
        for (int it = 0; it < points.size(); ++it)
        {
            printWriter.printf("%.4f \t %.4f \n",points.get(it).x_value, points.get(it).y_value);
        }
        printWriter.print("\n\n");
    }

    private void udpate_summ()//обновление значения суммы популяции
    {
        summ = 0; 
        for (int it = 0; it < points.size(); ++it)
        {
            summ += points.get(it).y_value;
        }
    }

    public GA(int strength, double precision, int step_count, double left, double right, double cros_prob, double mut_prob) //конструктор главного класса
    {
        step_count_ = step_count;
        right_ = right;
        left_ = left;
        summ = 0;

        bit_number = (int)Math.ceil( 
            Math.log((right - left) / precision) /
            Math.log(2)
            ); //вычисление количества бит в двоичном представлении

        for (int it = 0; it < strength; ++it) //инициализация начальной популяции
        {
            point temp = new point();
            temp = randomizer();
            points.addElement(temp);
            summ += temp.y_value;
        } 

        int it_count = 0;
        int it_flag = 0;
        try {
            FileWriter fileWriter = new FileWriter("out/points.dat");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            Collections.sort(points); 
            for (int it = 0; it < step_count; ++it) //основной цикл (итерации изменяющие популяцию)
            {
            to_file(printWriter);
            reproduction(); //вызов оператора репродукции
            crossing(cros_prob); //вызов оператора кроссинговера
            mutation(mut_prob); //вызов оператора мутации
            screenings(strength, precision); //удаление точек
            }
            printWriter.close();
        }   catch (final IOException e) {
            e.printStackTrace();
        }
        System.out.printf("max = %.3f\n", points.lastElement().y_value);
        plot();
    }

    private void reproduction() //оператор репродукции
    {
        wheel_only_p.clear();//нужны, чтобы избежать скрещивания точки с собой
        wheel_temp_p.clear();//

        for (int it = 0; it < points.size(); ++it) //инициализаци колеса рулетки
        {
            int wheel;
            wheel = (int)Math.round( (points.get(it).y_value + Math.abs(points.get(0).y_value)) 
                                            * points.size() / (summ + points.size() * Math.abs(points.get(0).y_value)) );
            if (wheel != 0) 
            {
                wheel_only_p.addElement(it);
                for (int jt = 0; jt < wheel; ++jt)
                {
                wheel_temp_p.addElement(it);
                }
            }
        }
    }

    private void crossing(double probability) //оператор кроссинговера
    {
        Vector<String> used = new Vector<String>();
        for (int it = 0; it < wheel_only_p.size(); ++it)
        {
                if( new Random().nextDouble() <= probability) { //скрещивание происходит с некоторой вероятностью
                    int jt =  ThreadLocalRandom.current().nextInt(0, wheel_temp_p.size());
                    int find = used.indexOf(String.valueOf(wheel_only_p.get(it)) +
                                         String.valueOf(wheel_temp_p.get(jt)));
                    while ( (wheel_only_p.get(it) == wheel_temp_p.get(jt)) || (find != -1) ) 
                        {
                            jt = ThreadLocalRandom.current().nextInt(0, wheel_temp_p.size());
                            find = used.indexOf(String.valueOf(wheel_only_p.get(it)) +
                                         String.valueOf(wheel_temp_p.get(jt)));
                        }    
                    used.addElement(String.valueOf(wheel_only_p.get(it)) + String.valueOf(wheel_temp_p.get(jt)));
                    used.addElement(String.valueOf(wheel_temp_p.get(jt)) + String.valueOf(wheel_only_p.get(it)));
                    Vector<Integer> first_bin = new Vector<>(bit_number);
                    Vector<Integer> second_bin = new Vector<>(bit_number);
                    first_bin = points.get(wheel_only_p.get(it)).binary_x;
                    second_bin = points.get(wheel_temp_p.get(jt)).binary_x;
                    int rand = ThreadLocalRandom.current().nextInt(1, bit_number);;
                    for (int k = rand; k < bit_number; ++k)//непосредственно само скрещивание
                    {
                        int swap_ = first_bin.get(k);
                        first_bin.set(k, second_bin.get(k)); 
                        second_bin.set(k, swap_);
                    }
                    point first_point = new point();
                    point second_point = new point();        
                    first_point = init_point(first_bin);
                    second_point = init_point(second_bin);
                    points.addElement(first_point);
                    points.addElement(second_point);
                    
                }
         }
    }
    
    private void mutation(double probability) //оператор мутации
    {
        for (int it = 0; it < wheel_temp_p.size(); ++it)
        {
            if( new Random().nextDouble() <= probability) {//мутация происходит с некоторой вероятностью
            int k =  (int)(Math.random() * (bit_number));
            Vector<Integer> temp = points.get(wheel_temp_p.get(it)).binary_x;
            if (temp.get(k) == 0) temp.set(k, 1);
            else temp.set(k, 0);
            point new_point = new point();
            new_point = init_point(temp);                 
            points.addElement(new_point);
            }
        }
    }

    private void screenings(double strength, double precision) //удаление точек до тех пор пока не останется изначальное количество точек
    {
        //точки сортируются по неубыванию, после чего первые элементы удаляются
        Collections.sort(points); 
        while (points.size() > strength)
        {
            points.remove(0);
        }
        udpate_summ();
    }

    public static void main(String[] args) //работа программы
    {
        GA test = new GA(40, //размер популяции 
        0.000001, //точность
        50, //количество итераций
        -20, -3.1, //границы
        0.5, //вероятность кроссинговера
        0.1 //вероятность мутации
        );
    }
}
