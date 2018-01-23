#/bin/bash

for i in {1..8}; do
#	cd ${ZOOKEEPER_HOME}; 
#	zooPath=`pwd -P`;
#	echo "${i}" > ./data/myid;	
#	scp -r -P 10022 ${zooPath} team1@sn0${i}.ib:~/seokwoo/;
	
	cd ${STORM_HOME};
	stormPath=`pwd -P`;
	ssh -p 10022 team1@sn0${i}.ib "rm -rf /home/team1/seokwoo/apache-storm-2.0.0-SNAPSHOT";
	scp -r -P 10022 ${stormPath} team1@sn0${i}.ib:~/seokwoo/;
done

