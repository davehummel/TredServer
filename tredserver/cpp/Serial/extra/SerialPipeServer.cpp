#include <stdio.h>

#include <string.h>

#include <fcntl.h>

#include <errno.h>

#include <termios.h>
//#include <unistd.h>
#include <iostream>
//#include <exception>
#include "mraa.hpp"

int set_interface_attribs (int fd, int speed, int parity)
{
        struct termios tty;
        memset (&tty, 0, sizeof tty);
        if (tcgetattr (fd, &tty) != 0)
        {
                std::cout << "error "<<errno<<" from tcgetattr\n";
                return -1;
        }

        cfsetospeed (&tty, speed);
        cfsetispeed (&tty, speed);

        tty.c_cflag = (tty.c_cflag & ~CSIZE) | CS8;     // 8-bit chars
        // disable IGNBRK for mismatched speed tests; otherwise receive break
        // as \000 chars
        tty.c_iflag &= ~IGNBRK;         // disable break processing
        tty.c_lflag = 0;                // no signaling chars, no echo,
                                        // no canonical processing
        tty.c_oflag = 0;                // no remapping, no delays
        tty.c_cc[VMIN]  = 0;            // read doesn't block
        tty.c_cc[VTIME] = 5;            // 0.5 seconds read timeout

        tty.c_iflag &= ~(IXON | IXOFF | IXANY); // shut off xon/xoff ctrl

        tty.c_cflag |= (CLOCAL | CREAD);// ignore modem controls,
                                        // enable reading
        tty.c_cflag &= ~(PARENB | PARODD);      // shut off parity
        tty.c_cflag |= parity;
        tty.c_cflag &= ~CSTOPB;
        tty.c_cflag &= ~CRTSCTS;


        if (tcsetattr (fd, TCSANOW, &tty) != 0)
        {
                std::cout <<  "error " << errno << " from tcsetattr\n";
                return -1;
        }
        tcflush(fd, TCIOFLUSH);
        return 0;
}

void set_blocking (int fd, int should_block)
{
        struct termios tty;
        memset (&tty, 0, sizeof tty);
        if (tcgetattr (fd, &tty) != 0)
        {
                std::cout<< "error " << errno << " from tggetattr";
                return;
        }

        tty.c_cc[VMIN]  = should_block ? 1 : 0;
        tty.c_cc[VTIME] = 255;            // 0.5 seconds read timeout

        if (tcsetattr (fd, TCSANOW, &tty) != 0)
                std::cout << "error " << errno <<  "setting term attributes";
}


int main() {
	std::cout << "Starting Serial Port\n";

	std::cout.flush();

	int fd1=open("/dev/ttyMFD1", O_RDWR | O_NOCTTY | O_SYNC);


	if (fd1 == -1 )
	{
				std::cout << "Error while opening fd?"
						<< std::endl;
				std::terminate();
	}

	set_interface_attribs(fd1,B3500000 ,0);
	set_blocking(fd1,1);
	// Clear all flags
	fcntl(fd1,F_SETFL,0);

	bool isActive = true;
	char buffer[255];
	char input[255];
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = 100;
	fd_set set;
	 FD_ZERO(&set); /* clear the set */
	 FD_SET(fd1, &set); /* add our file descriptor to the set */
	uint8_t inCharCount=0;
	while(isActive){
		bool done = false;
		uint8_t count = 0;
		int cycles = 0;
		while(!done){
			cycles++;
			std::cout<< "." ;
			if (select(fd1+1,&set,NULL,NULL, &timeout)>0){
					count = read(fd1,buffer,255);
					std::cout << "(" << (int)cycles<< ")" << "\n";
					cycles = 0;
				//	count = dev->read(buffer,255);
					if (count > 0){
						for (uint8_t j = 0; j < count ; j++){
							char x = input[inCharCount] = buffer[j];
							if (x == '\n' || x == '\r'){
								input[inCharCount] = '\0';
								done = true;
								break;
							}
							inCharCount++;
						}
					}
				}
		}
		std::cout << input << '\n';
		inCharCount = 0;
	}

  return 0;
}
