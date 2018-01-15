/*
 * JNISerial.cpp
 *
 *  Created on: Sep 8, 2015
 *      Author: dmhum_000
 */

#include <stdio.h>

#include <mraa.h>
#include <fcntl.h>
#include <string.h>
#include <errno.h>

#include <termios.h>

#include <iostream>
#include "JNISerial.h"

int fd1;

int set_interface_attribs (int fd, int speed, int parity)
{
        struct termios tty;
        memset (&tty, 0, sizeof   tty);
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
     
	tty.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
        tty.c_oflag = 0;                // no remapping, no delays
        tty.c_cc[VMIN]  = 0;            // read doesn't block
        tty.c_cc[VTIME] = 5;            // 0.5 seconds read timeout

        tty.c_iflag &= ~(IXON | IXOFF | IXANY | ICRNL | IGNCR | INLCR); // shut off xon/xoff ctrl

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

int set_blocking (int fd, int should_block)
{
        struct termios tty;
        memset (&tty, 0, sizeof tty);
        if (tcgetattr (fd, &tty) != 0)
        {
                std::cout<< "error " << errno << " from tggetattr";
                return -1;
        }

        tty.c_cc[VMIN]  = should_block ? 1 : 0;
        tty.c_cc[VTIME] = 255;            // 0.5 seconds read timeout

        if (tcsetattr (fd, TCSANOW, &tty) != 0){
                std::cout << "error " << errno <<  "setting term attributes";
                return -1;
        }
        return 0;
}



JNIEXPORT jstring JNICALL Java_me_davehummel_tredserver_serial_jniserial_JNISerial_setup  (JNIEnv *env, jclass cls){
	std::cout << "Starting Serial Port\n";
	std::cout.flush();
	fd1=open("/dev/ttyMFD1", O_RDWR | O_NOCTTY | O_SYNC);
	if (fd1<=0)
		return env->NewStringUTF( "Failed to create file descriptor.");
	if (set_interface_attribs(fd1,B3500000 ,0)!=0){
		return env->NewStringUTF("Failed to set attributes");
	}
	if (set_blocking(fd1,1)!=0){
		return env->NewStringUTF("Failed to set blocking");
	}

	fd_set set;
	 FD_ZERO(&set); /* clear the set */
	 FD_SET(fd1, &set); /* add our file descriptor to the set */

	// Clear all flags
	fcntl(fd1,F_SETFL,0);
	return 0;
}


uint8_t buffer[1024];
int bufferStart = 0;
int bufferEnd = 0;
uint8_t currentMsg[255];
int currentLen = 0;
int msgLen = 0;

bool reloadBuffer(){
	//std::cout<< "Loading DATA!";
	bufferEnd = read(fd1,buffer,1024);
	//std::cout<< "["<<bufferEnd<<"]<";
	//for (int i = 0 ; i < bufferEnd ; i++){
	//	std::cout << (int)buffer[i] << " ";
	//}
	//std::cout<<">\n";
	bufferStart = 0;
	if (bufferEnd == -1)
		return false;
	else
		return true;
}

JNIEXPORT jbyteArray JNICALL Java_me_davehummel_tredserver_serial_jniserial_JNISerial_readLine (JNIEnv *env, jclass clas){
	while (true){
		if (msgLen == 0 || currentLen < msgLen){
			if (bufferStart>=bufferEnd){
				if (!reloadBuffer()){
					//std::cout << "Bad reload";
					bufferEnd = 0;
					bufferStart = 0;
					return 0;
				}
			}
			if (msgLen == 0){
				msgLen = buffer[bufferStart];
				//std::cout << "new message with len:"<<(int)msgLen;

				currentLen = 0;
				bufferStart++;
				if (msgLen == 0 )
					return env->NewByteArray((jsize)0);
			}
			int bufferSpace = bufferEnd - bufferStart;
			int msgSpace = msgLen - currentLen;

			if (bufferSpace > msgSpace )
				bufferSpace = msgSpace;
			//std::cout << "Copying bytes from :"<<(int)bufferStart<<" for "<<(int)bufferSpace;

			memcpy (currentMsg+currentLen,buffer+bufferStart,bufferSpace);
			currentLen+=bufferSpace;
			bufferStart+=bufferSpace;

			//std::cout << "msg size = "<<(int)currentLen<< " of "<<(int) msgLen<<"\n<";
			//std::cout<<">\n";

		} else {
			//std::cout<<"Sending " << msgLen << "\n";
			jbyteArray jout =  env->NewByteArray(msgLen);
			env->SetByteArrayRegion(jout,0,msgLen,(jbyte*)currentMsg);
			msgLen = 0;
			return jout;
		return 0;
		}

	}

}



JNIEXPORT jstring JNICALL Java_me_davehummel_tredserver_serial_jniserial_JNISerial_write  (JNIEnv *env, jclass clas, jstring string){
	int len = env->GetStringUTFLength(string);
	const char* text = env->GetStringUTFChars(string, 0);
	write(fd1,text,len);
	env->ReleaseStringUTFChars(string,text);
	return 0;
}

JNIEXPORT void JNICALL Java_me_davehummel_tredserver_serial_jniserial_JNISerial_close
  (JNIEnv *env, jclass clas){
  	close(fd1);

  }
