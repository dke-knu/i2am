#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <fcntl.h>
#include <time.h>

#define MAX 1048576

void error_handling(char *message);

int main(int argc, char *argv[])
{
	int serv_sock;
	int clnt_sock;

	struct sockaddr_in serv_addr;
	struct sockaddr_in clnt_addr;
	socklen_t clnt_addr_size;

	char message[MAX];
	
	int option=1;
	int str_len;
	int fd;
	int check = 1;
	long long total=0;
	struct timespec t1, t2;
	double t11=0, t22=0;

	if(argc!=3){
		printf("Usage : %s <port> <file locate>\n", argv[0]);
		exit(1);
	}
	
	serv_sock=socket(PF_INET, SOCK_STREAM, 0);
	if(serv_sock == -1)
		error_handling("socket() error");
	
	if( setsockopt(serv_sock, SOL_SOCKET, SO_REUSEADDR, (void*)&option, sizeof(option)) )
		error_handling("setsockopt() error");

	memset(&serv_addr, 0, sizeof(serv_addr));
	serv_addr.sin_family=AF_INET;
	serv_addr.sin_addr.s_addr=htonl(INADDR_ANY);
	serv_addr.sin_port=htons(atoi(argv[1]));
	
	if(bind(serv_sock, (struct sockaddr*) &serv_addr, sizeof(serv_addr))==-1 )
		error_handling("bind() error"); 
	
	if(listen(serv_sock, 5)==-1)
		error_handling("listen() error");
	
	clnt_addr_size=sizeof(clnt_addr);  
	clnt_sock=accept(serv_sock, (struct sockaddr*)&clnt_addr,&clnt_addr_size);
	if(clnt_sock==-1)
		error_handling("accept() error");  
	
	fd = open(argv[2],O_WRONLY|O_CREAT|O_TRUNC,00700);
	if(fd == -1) error_handling("open() error");
	
	clock_gettime(CLOCK_MONOTONIC, &t1);
	while( (str_len=read(clnt_sock,message,MAX)) != 0)
	{
		write(fd,message,str_len);
		total += str_len;
	}
	close(fd);
	clock_gettime(CLOCK_MONOTONIC, &t2);
	t11 = t1.tv_sec + (double)t1.tv_nsec/1000000000.0;
	t22 = t2.tv_sec + (double)t2.tv_nsec/1000000000.0;

	printf("%lf MB/s\n", total/(t22-t11)/1024.0/1024.0);	
	close(clnt_sock);
		
	close(serv_sock);
	return 0;
}

void error_handling(char *message)
{
	fputs(message, stderr);
	fputc('\n', stderr);
	exit(1);
}

