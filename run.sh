#!/bin/bash

cd client/build/classes
java \
    -classpath ../../../deob-annotations/build/deob-annotations.jar:../../../signlink/build/signlink.jar:../../../lib/jogl-all.jar:../../../lib/gson.jar:../../../lib/gluegen-rt.jar:. \
    rt4.client
cd ../../..
