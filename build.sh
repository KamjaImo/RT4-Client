#!/bin/bash

javac \
    -Xlint:none \
    -sourcepath deob-annotations/src \
    -classpath . \
    -d deob-annotations/build/classes \
    deob-annotations/src/main/java/org/openrs2/deob/annotation/*.java

cd deob-annotations/build/classes
jar cf ../deob-annotations.jar *
cd ../../..

javac \
    -Xlint:none \
    -sourcepath signlink/src \
    -classpath deob-annotations/build/deob-annotations.jar:lib/jogl-all.jar \
    -d signlink/build/classes \
    signlink/src/main/java/rt4/*.java

cd signlink/build/classes
jar cf ../signlink.jar *
cd ../../..

javac \
    -Xlint:none \
    -sourcepath client/src \
    -classpath deob-annotations/build/deob-annotations.jar:signlink/build/signlink.jar:lib/jogl-all.jar:lib/gson.jar:lib/gluegen-rt.jar \
    -d client/build/classes \
    client/src/main/java/rt4/*.java client/src/main/java/plugin/*.java client/src/main/java/plugin/annotations/*.java client/src/main/java/plugin/api/*.java

cd client/build/classes
jar cfm ../client.jar ../manifest.mf  *
cd ../../..
