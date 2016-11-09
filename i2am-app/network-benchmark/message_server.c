#include <stdio.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>


#define MAX 1024

void error_handling(char *message)
{
	fputs(message, stderr);
	fputc('\n', stderr);
	exit(1);
}

int main(int argc, char* argv[])
{
	int serv_sock;
	int clnt_sock;

	int option=1;

	struct sockaddr_in serv_addr;
	struct sockaddr_in clnt_addr;
	socklen_t clnt_addr_size;	
	
	int fd;
	int str_len;

	char buffer[MAX];	
	int count=0;

	if(argc!=3) {
		printf("Usage : %s <port> <file locate>\n", argv[0]);
		exit(1);
	}
	
	serv_sock=socket(PF_INET, SOCK_STREAM, 0);
	if(serv_sock == -1)
		error_handling("socket() error");
	
	if(setsockopt(serv_sock, SOL_SOCKET, SO_REUSEADDR, (void *)&option, sizeof(option)))
		error_handling("setsockopt() error");

	memset(&serv_addr, 0, sizeof(serv_addr));
	serv_addr.sin_family=AF_INET;
	serv_addr.sin_addr.s_addr=htonl(INADDR_ANY);
	serv_addr.sin_port=htons(atoi(argv[1]));

	if(bind(serv_sock, (struct sockaddr*)&serv_addr, sizeof(serv_addr))==-1 )
		error_handling("bind() error");

	if(listen(serv_sock, 5)==-1)
		error_handling("listen() error");
	
	clnt_addr_size=sizeof(clnt_addr);
	while(1)
	{
		clnt_sock=accept(serv_sock, (struct sockaddr*)&clnt_addr, &clnt_addr_size);
		if(clnt_sock==-1)
			error_handling("accept() error");
		read(clnt_sock, buffer, 100);
		fd = open(buffer/*argv[2]*/, O_RDONLY);
		if(fd == -1)
			error_handling("open() error");
		while( (str_len=read(fd, buffer, MAX)) > 0)
		{
			write(clnt_sock, buffer, str_len);
		}
		close(fd);
		close(clnt_sock);
	}

	close(serv_sock);
	return 0;
}
