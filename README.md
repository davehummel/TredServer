# TredServer
C L 20 BEGIN 200 15 2000

C L 20 OFF

C V 500 BEGIN 0 5000 100

C P 10 MOVE 1 512
C P 10 MOVE 2 512
C P 10 MOVE 2 300
C P 10 MOVE 2 400

C P 10 ON
C P 10 OFF

C P 10 MOVE 1 0
C P 10 MOVE 1 1023

C P 10 BEGIN 5 200 400 624 1
C P 10 BEGIN 50 20 0 1023 1
C P 10 BEGIN 512 20 0 1023 2

C P 10 BEGIN steps 100 delay:20 interval:20 from:357 to:667 attemps 2 program(optional) 9

C N 10 ZERO 20 20

C V 500 BEGIN 0 5000 0
C N 10 MOVE 100 101 1000
C N 10 MOVE -100 101 2000
C N 10 DISCOVER
C N 10 TURN 2 2000
C N 10 TURN -2 2000
C N 10 TURN -10 2000

C N 10 TURN 90 5000
C N 10 TURN -90 5000
C N 10 TURN 180 5000
C G 10 HEADING

// Echo system
//            Start delay, interval, stereo delay, sample count, optional mute

C S 30 BEGIN 0 2000 40 0
C S 100 STOP

C L 20 BEGIN 40 20 10
C S 30 BEGIN 200 200 100 10


// MOTOR CONTROLLER
C M 100 MOVE F0 F255
C M 100 MOVE F255 F0
C M 100 MOVE R255 R255
C M 100 MOVE F255 F100
C M 100 MOVE F255 R255
C M 100 MOVE R100 R100
C M 100 MOVE F0 R0

// Set Motor acceleration rate
C M 100 ACCEL 40
// Get Motor Current
C M 100 CURRENT
// Get Motor ERror
C M 100 ERROR
C M 100 LIMIT 9
C M 100 LIMITR 4
C M 100 READ 8
C M 100 READ 9
C M 100 READ 10
C M 100 READ 11

C M 100 BREAK 100 100

P 0 C L 20 BEGIN 200 20 400;K 1;C P 10 MOVE 1 400

C G 10 ZERO
C G 10 CALMAG
C G 10 QUAT
C G 10 HEADING
C G 20 LIA
C G 10 TEMP
C G 10 ALTITUDE
C G 10 GRAV
C G 10 VEL 10 10
C G 10 RAW MGA
C G 10 RAW A
C G 10 ZEROA -3,0,10
C G 10 ZEROA 0,0,0

screen /dev/ttyMFD1 460800
screen /dev/ttyMFD1 921600

echo 182 > /sys/class/gpio/export
echo out > /sys/class/gpio/gpio182/direction
echo 13 > /sys/class/gpio/export
echo out > /sys/class/gpio/gpio13/direction

echo 0 > /sys/class/gpio/gpio182/value
echo 0 > /sys/class/gpio/gpio13/value

echo 1 > /sys/class/gpio/gpio182/value
echo 1 > /sys/class/gpio/gpio13/value
