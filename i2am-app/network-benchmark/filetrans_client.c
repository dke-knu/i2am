#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <fcntl.h>
#include <netinet/tcp.h>

#define MAX 1048576

void error_handling(char *message);

ssize_t fileRead(int fd, void *buf, size_t len)
{
	return read(fd, buf, len);
}
ssize_t networkWrite(int fd, const void *buf, size_t len)
{
	return write(fd,buf,len);
}

int main(int argc, char* argv[])
{
	int sock;
	struct sockaddr_in serv_addr;
	char message[MAX];
	int str_len;
	int fd;
	int flag = 1;

	if(argc!=4){
		printf("Usage : %s <IP> <port> <file locate>\n", argv[0]);
		exit(1);
	}
	
	sock=socket(PF_INET, SOCK_STREAM, 0);
	if(sock == -1)
		error_handling("socket() error");
	
	setsockopt(sock,IPPROTO_TCP, TCP_NODELAY, (char *)&flag,sizeof(int));	
	memset(&serv_addr, 0, sizeof(serv_addr));
	serv_addr.sin_family=AF_INET;
	serv_addr.sin_addr.s_addr=inet_addr(argv[1]);
	serv_addr.sin_port=htons(atoi(argv[2]));
		
	if(connect(sock, (struct sockaddr*)&serv_addr, sizeof(serv_addr))==-1) 
		error_handling("connect() error!");
	
	fd = open(argv[3], O_RDONLY);
	if(fd == -1)
		error_handling("open() error");
	while( (str_len=fileRead(fd, message, MAX)) > 0)
	{
		networkWrite(sock, message, str_len);
		memset(message, 0, MAX);
	}
	
	close(fd);
	close(sock);
	return 0;
}

void error_handling(char *message)
{
	fputs(message, stderr);
	fputc('\n', stderr);
	exit(1);
}

