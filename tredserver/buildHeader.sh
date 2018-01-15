#!/bin/sh
cd target/classes
javah -jni me.davehummel.tredserver.serial.jniserial.JNISerial
mv me_davehummel_tredserver_serial_jniserial_JNISerial.h ../../cpp/Serial/src/JNISerial.h
