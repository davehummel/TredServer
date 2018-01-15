#!/bin/sh
echo 182 > /sys/class/gpio/export
echo out > /sys/class/gpio/gpio182/direction
echo 13 > /sys/class/gpio/export
echo out > /sys/class/gpio/gpio13/direction

echo 0 > /sys/class/gpio/gpio182/value
echo 0 > /sys/class/gpio/gpio13/value