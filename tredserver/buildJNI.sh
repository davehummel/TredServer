#!/bin/sh
gcc -o JNISerial.so -lstdc++ -shared -I/home/root/java/include -I/home/root/java/include/linux cpp/Serial/src/JNISerial.cpp
mv JNISerial.so JNI/JNISerial.so

