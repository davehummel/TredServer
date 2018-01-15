#!/bin/sh
if [ -z $2 ]
then
    java -cp target/tredserver-1.0-SNAPSHOT-jar-with-dependencies.jar "me.davehummel.tredserver.$1"
else
    java -cp target/tredserver-1.0-SNAPSHOT-jar-with-dependencies.jar "me.davehummel.tredserver.$1" $2
fi
