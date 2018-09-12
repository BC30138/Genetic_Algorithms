#sudo chmod 755 compNstart.bat
javac -sourcepath src -d bin -classpath lib/JavaPlot.jar src/com/bc30138/geneticalg/GA.java
java -classpath bin:lib/JavaPlot.jar com.bc30138.geneticalg.GA