#!/bin/bash

CLASSPATH=.:lib/jogl-all.jar:lib/gson.jar:lib/gluegen-rt.jar

# Build deob-annoations library
javac \
    -Xlint:none \
    -sourcepath deob-annotations/src \
    -classpath $CLASSPATH \
    -d deob-annotations/build/classes \
    deob-annotations/src/main/java/org/openrs2/deob/annotation/*.java

cd deob-annotations/build/classes
jar cf ../deob-annotations.jar *
cd ../../..
CLASSPATH=$CLASSPATH:deob-annotations/build/deob-annotations.jar

# Build signlink library
javac \
    -Xlint:none \
    -sourcepath signlink/src \
    -classpath $CLASSPATH \
    -d signlink/build/classes \
    signlink/src/main/java/rt4/*.java

cd signlink/build/classes
jar cf ../signlink.jar *
cd ../../..
CLASSPATH=$CLASSPATH:signlink/build/signlink.jar

# Build plugin library
find client/src -iname *.java > plugin-lib.txt
javac \
    -Xlint:none \
    -sourcepath client/src \
    -classpath $CLASSPATH \
    -d client/build/plugin-lib/classes \
    @plugin-lib.txt
rm plugin-lib.txt

cd client/build/plugin-lib/classes
jar cf ../plugin-lib.jar *
cd ../../../..
CLASSPATH=$CLASSPATH:client/build/plugin-lib/plugin-lib.jar

# Build main client executable
javac \
    -Xlint:none \
    -sourcepath client/src \
    -classpath $CLASSPATH \
    -d client/build/classes \
    client/src/main/java/rt4/*.java

cd client/build/classes
jar cfm ../client.jar ../manifest.mf  *
cd ../../..

# Build optional plugins
find plugin-playground/src -iname *.java > plugins-java.txt
javac \
    -Xlint:none \
    -sourcepath plugin-playground/src \
    -classpath $CLASSPATH \
    -d client/build/classes/plugins \
    @plugins-java.txt
rm plugins-java.txt

# Kotlin issues on my machine, you might be fine
# find plugin-playground/src -iname *.kt > plugins-kt.txt
# kotlinc \
#     -Xlint:none \
#     -classpath $CLASSPATH
#     -d client/plugins \
#     @plugins-kt.txt
# rm plugins-kt.txt