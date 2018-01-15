#!/bin/sh
if [ "$(git pull)" != "Already up-to-date." ];then
    mvn compile assembly:single
fi