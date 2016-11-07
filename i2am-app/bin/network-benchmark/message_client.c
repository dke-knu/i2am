#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <fcntl.h>
#include <netinet/tcp.h>
#include <time.h>
#include <pthread.h>

#define MAX 1024

int chk = 0;

void error_handling(char *message)
{
	fputs(message, stderr);
	fputc('\n', stderr);
	exit(1);
}

void *t_function(void *data)
{
	sleep(60);
	chk = 1;
}

int main(int argc, char* argv[])
{
	int sock;
	int fd;

	struct sockaddr_in serv_addr;
	
	int str_len;

	char buffer[MAX];
	int count = 0;
	
	struct timeval tv1, tv2;
	struct timespec t1, t2;
	long double tv, t;

	pthread_t p_th;
	int th_id;

	if(argc!=4) {
		printf("Usage : %s <ip> <port> <file locate>\n", argv[0]);
		exit(1);
	}
	
	th_id = pthread_create(&p_th, NULL, t_function, NULL);
	//gettimeofday(&tv1, 0);
	//clock_gettime(CLOCK_MONOTONIC, &t1);
	while(/*count < 100000*/chk != 1 )
	{
		sock = socket(PF_INET, SOCK_STREAM, 0);
		if(sock==-1)
			error_handling("socket() error");

	        memset(&serv_addr, 0, sizeof(serv_addr));
	        serv_addr.sin_family=AF_INET;
	        serv_addr.sin_addr.s_addr=inet_addr(argv[1]);
	        serv_addr.sin_port=htons(atoi(argv[2]));

		if(connect(sock, (struct sockaddr*)&serv_addr, sizeof(serv_addr))==-1)
			continue;//error_handling("connect() error!");
		write(sock, argv[3], 100);
		fd=open(argv[3], O_WRONLY|O_CREAT|O_TRUNC,00700);
		if(fd==-1) error_handling("open() error");

		while( (str_len=read(sock,buffer,MAX)) > 0)
		{
			write(fd, buffer, str_len);
		}
		close(sock);
		close(fd);
		count++;
	}
	//gettimeofday(&tv2, 0);
	//clock_gettime(CLOCK_MONOTONIC, &t2);
	
	//tv = (long double)(tv2.tv_sec - tv1.tv_sec) + (long double)(tv2.tv_usec-tv1.tv_usec)/1000000.0;
	//t = (long double)(t2.tv_sec - t1.tv_sec) + (long double)(t2.tv_nsec - t1.tv_nsec)/1000000000.0;
	//printf(" %llf\n %llf\n",tv, t);
	printf("%10d ",count);
	return 0;
}
