#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<unistd.h>
#include<arpa/inet.h>
#include<sys/socket.h>
void error_handling(char *message);

int main(int argc, char* argv[])
{
	int i;
	int serv_sock;
	int clnt_sock;
	int state, len;
	int snd_buf;
	int rcv_buf;
	int size;
	int tem;
	struct sockaddr_in serv_addr;
	struct sockaddr_in clnt_addr;
	socklen_t clnt_addr_size;
	int option=1;
	long long int a;
	char *message;
	
	if(argc != 3)
	{
		printf("Usage: %s <port> <size> \n", argv[0]);
		exit(1);
	}
	
	serv_sock=socket(PF_INET, SOCK_STREAM,0);
	if(serv_sock==-1)
		error_handling("socket() error");
	state=setsockopt(serv_sock, SOL_SOCKET, SO_REUSEADDR, (void*)&option, sizeof(option));
	if(state)
		error_handling("setsockopt() error");
	
	memset(&serv_addr, 0, sizeof(serv_addr));
	serv_addr.sin_family=AF_INET;
	serv_addr.sin_addr.s_addr=htonl(INADDR_ANY);
	serv_addr.sin_port=htons(atoi(argv[1]));
	
	if(bind(serv_sock, (struct sockaddr*) &serv_addr, sizeof(serv_addr))==-1)
		error_handling("bind() error");
	
	if(listen(serv_sock, 5) ==-1)
		error_handling("listen() error");
	
	clnt_addr_size=sizeof(clnt_addr);
	clnt_sock=accept(serv_sock, (struct sockaddr*)&clnt_addr, &clnt_addr_size);
	if(clnt_sock==-1)
		error_handling("accept() error");
/*
	rcv_buf = snd_buf = atoi(argv[3]);
	state=setsockopt(clnt_sock, SOL_SOCKET, SO_RCVBUF, (void*)&rcv_buf, sizeof(rcv_buf));
	if(state)
		error_handling("setsockopt() error!");
	state=setsockopt(clnt_sock, SOL_SOCKET, SO_SNDBUF, (void*)&snd_buf, sizeof(snd_buf));
	if(state)
		error_handling("setsockopt() error!");*/
	len=sizeof(snd_buf);
        state=getsockopt(clnt_sock, SOL_SOCKET, SO_SNDBUF, (void*)&snd_buf, &len);
        if(state)
                error_handling("getsock() error");
        len=sizeof(rcv_buf);
        state=getsockopt(clnt_sock, SOL_SOCKET, SO_RCVBUF, (void*)&rcv_buf, &len);
        if(state)
                error_handling("getsockopt() error");

        printf("Input buffer size: %d \t", rcv_buf);
        printf("Output buffer size: %d\n", snd_buf);
	
	size=atoi(argv[2]);
	message = malloc(size);
	a=0;
	while(1)
	{
		tem=recv(clnt_sock, message, size, MSG_WAITALL);
		if(message[0]==1)
			break;
		a=a+tem;
	}
	printf("%lld\n", a);
		
	close(clnt_sock);
	close(serv_sock);
	free(message);
	return 0;
}

void error_handling(char *message)
{
	fputs(message, stderr);
	fputc('\n', stderr);
	exit(1);
}
