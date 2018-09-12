echo main-class: com.bc30138.geneticalg.GA>manifest.mf
echo class-path: lib/geneticalg.jar >>manifest.mf
cp lib/JavaPlot.jar lib/geneticalg.jar
jar -cmf manifest.mf GA.jar  -C bin .