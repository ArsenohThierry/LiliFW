javac -Xlint -cp "lib/servlet-api.jar" -d test $(find . -name "*.java")

rm -Rf test