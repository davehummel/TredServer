#!/bin/sh
if [ "$(git pull)" != "Already up-to-date." ];then
    mvn -DskipTests package
fi
mvn exec:java -Dexec.mainClass="me.davehummel.tredserver.SerialTest"
# mvn exec:java -Dexec.mainClass="io.netty.example.http.websocketx.server.WebSocketServer"
