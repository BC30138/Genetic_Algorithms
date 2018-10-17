#sudo chmod 755 compNstart.bat
javac -sourcepath src -d bin src/com/bc30138/ga2/*.java
java -classpath bin: com.bc30138.ga2.GA