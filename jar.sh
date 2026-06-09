#!/bin/bash

# Nettoyage
rm -rf out
mkdir -p out

# Compilation
javac -d out src/main/java/lilifw/FormControllerServlet.java

# Vérification
if [ $? -ne 0 ]; then
    echo "Erreur de compilation"
    exit 1
fi

# Création du JAR
jar cf lilifw.jar -C out .

echo "JAR créé : lilifw.jar"