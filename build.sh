mkdir ./out
find -name "*.java" > sources.txt
BUILD_COMMAND="javac -d ./out @sources.txt"
${BUILD_COMMAND} --add-exports=java.desktop/sun.awt=ALL-UNNAMED || \
  echo "Command failed, retrying assuming older Java build" && ${BUILD_COMMAND}
cd ./out
find ../src -name "*.png" -exec cp '{}' ./com/modsim/res/ \;
jar cfm ../ModuleSim-Test.jar ../src/META-INF/MANIFEST.MF ./
cd ../
rm ./sources.txt
