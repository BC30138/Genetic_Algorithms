#sudo chmod 755 compNstart.bat
javac -sourcepath src -d bin src/com/bc30138/ga/*.java
printf "\n --------result--------- \n "
java -classpath bin: com.bc30138.ga.GA
printf " --------result--------- \n"