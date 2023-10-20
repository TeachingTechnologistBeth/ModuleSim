mkdir ./out
find -name "*.java" > sources.txt
javac -d ./out @sources.txt
cd ./out
find ../src -name "*.png" -exec cp '{}' ./com/modsim/res/ \;
jar cfm ../ModuleSim-Test.jar ../src/META-INF/MANIFEST.MF ./
cd ../
rm ./sources.txt
