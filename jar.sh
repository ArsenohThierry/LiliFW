#!/bin/bash

# Nettoyage
rm -rf out
mkdir -p out

# Compilation
CLASSPATH="lib/servlet-api.jar"
for jar in lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done
javac -Xlint -cp "$CLASSPATH" -d out $(find . -name "*.java")

# Vérification
if [ $? -ne 0 ]; then
    echo "Erreur de compilation"
    exit 1
fi

# Création du JAR
jar cf lilifw.jar -C out .
mkdir -p output 
mv lilifw.jar output

rm ../TEST_liliFW/lib/lilifw.jar
cp output/lilifw.jar ../TEST_liliFW/lib/

echo "JAR créé : lilifw.jar"
rm -rf out