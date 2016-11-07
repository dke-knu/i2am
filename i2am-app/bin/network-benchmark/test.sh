for ((i=1;i<=100000000;i=i*10)); do
	echo $i;
	for((j=0;j<10;j++)); do
	
	c1="./message_client 192.168.0.107 9000 /var/tmp/"$i;
	c2="./message_client 192.168.1.107 9000 /var/tmp/"$i;
	$c1; $c2;
	echo;
	done;
done;
