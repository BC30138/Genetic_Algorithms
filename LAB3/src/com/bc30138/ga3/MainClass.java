package com.bc30138.ga3;

import java.util.Arrays;

class MainClass { // главный класс программы
    public static void main(String[] args) 
    {
        Travel test = new Travel(1000, 2, 0.5, 0.001); //инициализация объекта решения 
                                                       //задачи
        test.search(); // запуск алгоритма
        System.exit(0); 
    }
}