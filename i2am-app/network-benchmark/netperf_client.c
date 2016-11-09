#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<unistd.h>
#include<arpa/inet.h>
#include<sys/socket.h>
#include<time.h>
#include<pthread.h>
void error_handling(char *message);

void *thread_main(void *arg);

int ti = 0;

int main(int argc, char* argv[])
{
	int sock;
	int state, len;
	int rcv_buf=425984,snd_buf=425984;
	struct sockaddr_in serv_addr;
	char *message;
	int str_len;
	int size;
	struct timespec myclock[2];
	int i;
	long long int a;
	long double band;
	pid_t pid;	
	int thr_id;
	pthread_t p_thread; 
	int tim =10;

	if(argc!=4)
	{
		printf("Usage: %s <IP> <port> <size> \n", argv[0]);
		exit(1);
	}
	
	sock=socket(PF_INET, SOCK_STREAM, 0);
	if(sock == -1)
		error_handling("socket() error");
/*	snd_buf = rcv_buf = atoi(argv[4]);
	state=setsockopt(sock, SOL_SOCKET, SO_RCVBUF, (void*)&rcv_buf, sizeof(rcv_buf));
        if(state)
                error_handling("setsockopt() error!");
        state=setsockopt(sock, SOL_SOCKET, SO_SNDBUF, (void*)&snd_buf, sizeof(snd_buf));
        if(state)
                error_handling("setsockopt() error!");*/
        len=sizeof(snd_buf);
        state=getsockopt(sock, SOL_SOCKET, SO_SNDBUF, (void*)&snd_buf, &len);
        if(state)
                error_handling("getsock() error");
        len=sizeof(rcv_buf);
        state=getsockopt(sock, SOL_SOCKET, SO_RCVBUF, (void*)&rcv_buf, &len);
        if(state)
                error_handling("getsockopt() error");

        printf("Input buffer size: %d \t", rcv_buf);
        printf("Output buffer size: %d\n", snd_buf);


	memset(&serv_addr, 0, sizeof(serv_addr));
	serv_addr.sin_family=AF_INET;
	serv_addr.sin_addr.s_addr=inet_addr(argv[1]);
	serv_addr.sin_port=htons(atoi(argv[2]));
	
	if(connect(sock, (struct sockaddr*)&serv_addr, sizeof(serv_addr))==-1)
		error_handling("connect() error!");
	size=atoi(argv[3]);
	message = malloc(size);
	memset(message, 0, size);
	a=0;
	pthread_create(&p_thread, NULL, thread_main, (void *)&tim);
	while(ti != 1)
	{	
		a+=send(sock, message, size, 0);
	}
	message[0]=1;
	send(sock, message, size, 0);
	band=a/1024.0/1024.0/1024.0*8.0;
	band=band/10.0;

	printf("Message from server : %s \n Size: %lld\n", message, a);
	printf("Bandwidth : %llf Gbps\n N_Bytes: %lld\n",band, a);
	close(sock);
	free(message);
	return 0;
}

void error_handling(char *message)
{
	fputs(message, stderr);
	fputc('\n', stderr);
	exit(1);
}

void *thread_main(void *arg)
{
	sleep(10);
	ti = 1;
	printf("thread() : %d", ti);
	pthread_exit((void *) 0);
}
