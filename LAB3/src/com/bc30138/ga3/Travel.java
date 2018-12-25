package com.bc30138.ga3;

import java.io.File;
import java.awt.EventQueue;
import javax.swing.JFileChooser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Random;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

class City_Coord { // Город
    private double x_value; // x координата
    private double y_value; // y координата

    City_Coord(double x_value, double y_value) {
        set(x_value, y_value);
    }

    void set(double x_value, double y_value) { 
        this.x_value = x_value;
        this.y_value = y_value; 
    }

    double get_x() {
        return x_value;
    }

    double get_y() {
        return y_value;
    }
}

class Tour implements Comparable<Tour> { // Путь
    Integer[] tour_cities; // последовательный массив идентификаторов городов
    double tour_result; // стоимость пути

    void search_result(ArrayList<City_Coord> city_list) { // функция подсчета стоимости пути
        tour_result = 0;
        for (int it = 0; it < tour_cities.length - 1; ++it) {
            tour_result += Math.sqrt(Math.pow(city_list.get(tour_cities[it]).get_x() 
                                        - city_list.get(tour_cities[it + 1]).get_x() , 2) + 
                                    Math.pow(city_list.get(tour_cities[it]).get_y() 
                                        - city_list.get(tour_cities[it + 1]).get_y() , 2)
                                    );
        }
        // нужно брать в учет стоимость проезда от последнего города к первому
        tour_result += 
            Math.sqrt(Math.pow(city_list.get(tour_cities[tour_cities.length - 1]).get_x()
                                - city_list.get(tour_cities[0]).get_x() , 2) + 
                    Math.pow(city_list.get(tour_cities[tour_cities.length - 1]).get_y() 
                                - city_list.get(tour_cities[0]).get_y() , 2));
    }

    @Override
    public int compareTo(Tour o) // оператор сравнения туров
    {
        Tour e = (Tour) o;

        int result = Double.valueOf(this.tour_result).compareTo(Double.valueOf(e.tour_result));

        if (result < 0) {
            return -1;
        } else {
            if (result == 0) {
                return 0;
            } else
                return 1;
        }
    }
}

class Travel {
    File input_file; // входной файл
    private ArrayList<City_Coord> city_list; // список координат городов 
    private ArrayList<Tour> tours; // популяция 
    private int pop_size; // размер популяции
    private int it_number; // количество итераций
    private int time_of_searching; // максимальное время решения
    private double x_prob; // вероятность кроссовера
    private double mut_prob; // вероятность мутации
    private PrintWriter result_output; // выходной файл для записи стоимостей
    private PrintWriter output; // выходной файл для записи лучших особи за итерацию
    private PrintWriter gnuplot; // файл скрипта гнуплот
    private String best_file_path; // путь до файла содержащего оптимальное решение

    Travel(int pop_size, int time_of_searching, double x_prob, double mut_prob) // конструктор
    {        
        // инициализация переменных
        this.pop_size = pop_size; 
        this.time_of_searching = time_of_searching * 1000;
        this.x_prob = x_prob;
        this.mut_prob = mut_prob;
        it_number = 0;

        try { result_output = new PrintWriter(new File("data/result_output.dat"));
        } catch (IOException e) {}

        try { output = new PrintWriter(new File("data/output.dat"));
        } catch (IOException e) {}
        city_list = new ArrayList<>();
        tours = new ArrayList<>();
    
        try { 
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                JFileChooser file_open = new JFileChooser("./data/");
                file_open.showDialog(null, "Открыть входной файл");
                input_file = file_open.getSelectedFile();

                JFileChooser best_open = new JFileChooser("./data/");
                 best_open.showDialog(null, "Открыть файл для сравнения");
                File best_file = best_open.getSelectedFile();
                best_file_path = best_file.getPath();
                };
            });
        } catch (Exception e) { System.exit(1); }

        try {
            Scanner in = new Scanner(input_file);
            while(in.hasNextDouble()) {
            double tmp = in.nextDouble();
            city_list.add(new City_Coord(in.nextDouble(),in.nextDouble()));
            }
        }
        catch (Exception e) {
            System.exit(0);
        }
    }

    public void search() 
    {
        init_first(); 
        Collections.sort(tours);
        to_file();
        
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis()-startTime) < time_of_searching) {
            x_operator();
            mut_operator();
            to_file();
            result_output.println(tours.get(0).tour_result);
            ++it_number;
        }
        output.close();
        result_output.close();
        plot();
    } 

    private void init_first() // инициализация начальной популяции
    {
        ArrayList<Integer> not_used = new ArrayList<>();
        for (int it = 0; it < city_list.size() - 1; it++) {
            not_used.add(it + 1);
        }

        for (int it = 0; it < pop_size; ++it) {
            ArrayList<Integer> tmp_not_used = new ArrayList<>(not_used);
            Tour tour = new Tour();
            tour.tour_cities = new Integer[city_list.size()];
            tour.tour_cities[0] = 0;
            Random rnd = new Random(System.currentTimeMillis()); 
            for (int jt = 1; jt < tour.tour_cities.length; ++jt) {
                int choosed = rnd.nextInt(tmp_not_used.size());
                tour.tour_cities[jt] = tmp_not_used.get(choosed);
                tmp_not_used.remove(choosed);
            }
            tour.search_result(city_list);
            tours.add(tour);
            try {
                Thread.sleep(1);
            }
            catch (Exception e) {}
        }
    }

    private int generate_parent() //функция выбора родителя (турнир)
    {
        int first_cand = ThreadLocalRandom.current().nextInt(0, pop_size);
        int second_cand;
        do { second_cand = ThreadLocalRandom.current().nextInt(0, pop_size);
        } while (first_cand == second_cand);

        double prob = tours.get(first_cand).tour_result / (tours.get(first_cand).tour_result + tours.get(second_cand).tour_result);
        if (new Random().nextDouble() <= prob) {
            return first_cand; 
        } else return second_cand;
    }

    private void x_operator() // оператор кроссинговера
    {
        ArrayList<Tour> childs = new ArrayList<>(); 
        boolean used_flag[][] = new boolean[pop_size][pop_size];
        for (int it_x = 0; it_x < pop_size; ++it_x) {
            if (new Random().nextDouble() <= x_prob) { // скрещивание происходит 
                                                       // с некоторой вероятностью
                generate_parent();
                int first_parent_it; 
                int second_parent_it;
                do {
                    first_parent_it = generate_parent();
                    second_parent_it = generate_parent();
                }
                while ((first_parent_it == second_parent_it) 
                || used_flag[first_parent_it][second_parent_it]);
                    
                used_flag[first_parent_it][second_parent_it] = true;
                used_flag[second_parent_it][first_parent_it] = true;

                // генерация точек разреза
                int cut_first = ThreadLocalRandom.current().nextInt(1, city_list.size() - 1);
                int cut_second;
                do {
                    cut_second = ThreadLocalRandom.current().nextInt(cut_first, city_list.size());
                }
                while (cut_first == cut_second);
                
                int range = cut_second - cut_first + 1; 
                Integer[] middle_first = Arrays.copyOfRange(
                    tours.get(first_parent_it).tour_cities, cut_first, cut_second + 1); 
                //решил сделать реализацию такую, чтобы если перерезы совпадали, 
                //то замена происходила у одного элемента, иначе говоря не увидел 
                // ничего плохого в том, чтобы включить точку правого разрыва
                Integer[] middle_second = Arrays.copyOfRange(
                    tours.get(second_parent_it).tour_cities, cut_first, cut_second + 1);
                
                Tour child_first = new Tour(); // первый потомок
                Tour child_second = new Tour(); // второй потомок
                
                child_first.tour_cities = new Integer[city_list.size()];
                child_second.tour_cities = new Integer[city_list.size()];

                child_first.tour_cities[0] = 0;
                child_second.tour_cities[0] = 0;    

                // заполнение потомков до первого разреза
                for (int it_child = 1; it_child < cut_first; ++it_child) {
                    int middle_first_it = Arrays.asList(middle_first).indexOf(
                                            tours.get(second_parent_it).tour_cities[it_child]); 
                    if (middle_first_it != -1) { 
                        while (middle_first_it != -1) {
                            child_second.tour_cities[it_child] = 
                                new Integer(middle_second[middle_first_it]);
                            middle_first_it =
                                 Arrays.asList(middle_first).indexOf(
                                        child_second.tour_cities[it_child]);
                        }
                    } else child_second.tour_cities[it_child] =
                                 new Integer(tours.get(second_parent_it).tour_cities[it_child]);
                    
                    int middle_second_it = Arrays.asList(middle_second).indexOf(
                                            tours.get(first_parent_it).tour_cities[it_child]); 
                    if (middle_second_it != -1) { 
                        while (middle_second_it != -1) {
                            child_first.tour_cities[it_child] = 
                                            new Integer(middle_first[middle_second_it]);
                            middle_second_it = Arrays.asList(middle_second).indexOf(
                                                    child_first.tour_cities[it_child]);
                        }
                    } else child_first.tour_cities[it_child] = 
                                new Integer(tours.get(first_parent_it).tour_cities[it_child]);
                }

                // заполнение потомков между разрезами
                for (int it_m = cut_first; it_m < cut_second + 1; ++it_m)
                {
                    child_first.tour_cities[it_m] =  new Integer(middle_second[it_m - cut_first]);
                    child_second.tour_cities[it_m] =  new Integer(middle_first[it_m - cut_first]);
                }

                // заполнение потомков после второго
                for (int it_child = cut_second + 1; it_child < city_list.size(); ++it_child) {
                    int middle_first_it = Arrays.asList(middle_first).indexOf(
                        tours.get(second_parent_it).tour_cities[it_child]); 
                    if (middle_first_it != -1) { 
                        while (middle_first_it != -1) {
                            child_second.tour_cities[it_child] = new Integer(
                                                middle_second[middle_first_it]);
                            middle_first_it = Arrays.asList(middle_first).indexOf(
                                                child_second.tour_cities[it_child]);
                        }
                    } else child_second.tour_cities[it_child] = 
                                new Integer(tours.get(second_parent_it).tour_cities[it_child]);
                    
                    int middle_second_it = Arrays.asList(middle_second).indexOf(tours.get(
                                                    first_parent_it).tour_cities[it_child]); 
                    if (middle_second_it != -1) { 
                        while (middle_second_it != -1) {
                            child_first.tour_cities[it_child] = 
                                                new Integer(middle_first[middle_second_it]);
                            middle_second_it = Arrays.asList(middle_second).indexOf(
                                                            child_first.tour_cities[it_child]);
                        }
                    } else child_first.tour_cities[it_child] = 
                                new Integer(tours.get(first_parent_it).tour_cities[it_child]);
                }
                
                child_first.search_result(city_list); 
                child_second.search_result(city_list);
                childs.add(child_first); 
                childs.add(child_second);
            }
        }
        // добавление потомков в популяцию
        tours.addAll(childs);
    }

    private void mut_operator() //оператор мутации
    {
        for (int it = 0; it < tours.size(); ++it) {
            // мутация происходит с некоторой вероятностью
            if (new Random().nextDouble() <= mut_prob) {
                Random random = new Random();

                int k1 = new Random(System.currentTimeMillis()).nextInt(city_list.size() - 1) + 1; 
                try {
                    Thread.sleep(1);
                } catch (Exception e) {}
                int k2;
                do {
                    k2 = new Random(System.currentTimeMillis()).nextInt(city_list.size() - 1) + 1; 
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {}
                } while (k1 == k2);
                Integer tmp = new Integer(tours.get(it).tour_cities[k1]);
                tours.get(it).tour_cities[k1] = tours.get(it).tour_cities[k2];
                tours.get(it).tour_cities[k2] = tmp;
                tours.get(it).search_result(city_list);
            }
        }

        Collections.sort(tours); // сортировка популяции
        while (tours.size() > pop_size) // обрезание худших особей
        {
            tours.remove(tours.size() - 1);
        }
    }

    private void to_file() // вывод в файл лучшего в популяции
    {
        for (int it = 0; it < city_list.size(); ++it)
        {
            output.println(city_list.get(tours.get(0).tour_cities[it]).get_x() + " " 
            + city_list.get(tours.get(0).tour_cities[it]).get_y());
        }
        output.println(city_list.get(tours.get(0).tour_cities[0]).get_x() + " " 
        + city_list.get(tours.get(0).tour_cities[0]).get_y());
        output.print("\n\n");
    }

    public void plot() // построение графика
    {
        try {gnuplot = new PrintWriter(new File("src/com/bc30138/ga3/script.gp")); 
        } catch (Exception e) {}

        gnuplot.println("set term gif animate delay 15 font 'Helvetica,18' size 1024,600");
        gnuplot.println("set xlabel 'X'");
        gnuplot.println("set ylabel 'Y'");
        gnuplot.println("set key spacing 1.5");
        gnuplot.println("set output 'searchprocess.gif'");
        gnuplot.println("set key right");
        gnuplot.println("do for [i=0:" + (it_number + 5)  + "] {");
        gnuplot.println("plot '" + best_file_path +
             "' u 1:2 w lines lw 2 lt rgb 'red' ti 'Opt result'," +
        " 'data/output.dat' index i u 1:2 w lines lw 1 lt rgb 'blue' ti 'Opt search'");
        gnuplot.println("}");

        gnuplot.close();

        final Runtime run = Runtime.getRuntime();
        try {
            String fpath = "gnuplot src/com/bc30138/ga3/script.gp";
            Process p = run.exec(fpath);
            int result = p.waitFor();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}