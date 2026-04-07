DEOB_SOURCES=$(shell find deob-annotations/src -type f -iname '*.java')
SIGNLINK_SOURCES=$(shell find signlink/src -type f -iname '*.java')
CLIENT_SOURCES=$(shell find client/src -type f -iname '*.java')
PLUGIN_SOURCES=$(shell find plugin-playground/src -type f -iname '*.java')
PLAYGROUND_SOURCES=$(shell find playground/src -type f -iname '*.java')

LIBS=$(shell find lib -type f -iname '*.jar')
DEOB=deob-annotations/build/deob-annotations.jar
SIGNLINK=signlink/build/signlink.jar
CLIENT=client/build/client.jar
PLUGINS=$(shell find plugin-playground/src -type d)

MANIFEST=manifest.mf
EARLY_CLASSPATH=.:$(shell echo $(LIBS) | tr ' ' ':'):$(DEOB):$(SIGNLINK)
CLASSPATH=.$(EARLY_CLASSPATH):$(CLIENT)

all: client plugins

client: $(CLIENT)

plugins: $(PLUGINS) $(PLUGIN_SOURCES) $(CLIENT)
	javac \
		-Xlint:none \
		-sourcepath plugin-playground/src \
		-classpath $(CLASSPATH) \
		-d client/build/classes/plugins \
		$(PLUGIN_SOURCES)

clean: 
	rm -r client/build/*
	rm -r deob-annotations/build/*
	rm -r plugin-playground/build/*
	rm -r signlink/build/*

$(DEOB): $(LIBS) $(DEOB_SOURCES)
	javac \
		-Xlint:none \
		-sourcepath deob-annotations/src \
		-classpath $(CLASSPATH) \
		-d deob-annotations/build/classes \
		$(DEOB_SOURCES)
	jar cf $(DEOB) -C deob-annotations/build/classes .
	
$(SIGNLINK): $(LIBS) $(DEOB) $(SIGNLINK_SOURCES)
	javac \
		-Xlint:none \
		-sourcepath signlink/src \
		-classpath $(EARLY_CLASSPATH) \
		-d signlink/build/classes \
		$(SIGNLINK_SOURCES)
	jar cf $(SIGNLINK) -C signlink/build/classes .

$(CLIENT): $(LIBS) $(DEOB) $(SIGNLINK) $(CLIENT_SOURCES)
	javac \
		-Xlint:none \
		-sourcepath client/src \
		-classpath $(CLASSPATH) \
		-d client/build/classes \
		$(CLIENT_SOURCES)
	jar cfm $(CLIENT) $(MANIFEST) -C client/build/classes .

.PHONY: all clean client plugins