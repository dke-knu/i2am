#!/bin/bash

netConfDir=$(cd /home/team1/seokwoo/storm-zookeeper-networkConf; pwd -P)

if [ $# -lt 1 ]; then
	echo "Usage: $0 [eth | ib | rdma]";
	exit 0;
fi

if [ "$1" == "eth" ]; then
	cp ${netConfDir}/storm-eth.yaml ${netConfDir}/storm.yaml;
	echo "cp ${netConfDir}/storm-eth.yaml ${netConfDir}/storm.yaml";
	echo "Change storm configuration to ethernet";

	cp ${netConfDir}/zoo-eth.cfg ${netConfDir}/zoo.cfg;
	echo "cp ${netConfDir}/zoo-eth.cfg ${netConfDir}/zoo.cfg";
	echo "Change zookeeper configuration to ethernet";

elif [ "$1" == "ib" ]; then
	cp ${netConfDir}/storm-ib.yaml ${netConfDir}/storm.yaml;
	echo "cp ${netConfDir}/storm-ib.yaml ${netConfDir}/storm.yaml";
	echo "Change storm configuration to infiniband";

	cp ${netConfDir}/zoo-ib.cfg ${netConfDir}/zoo.cfg;
	echo "cp ${netConfDir}/zoo-ib.cfg ${netConfDir}/zoo.cfg";
	echo "Change zookeeper configuration to infiniband";

elif [ "$1" == "rdma" ]; then
	cp ${netConfDir}/storm-rdma.yaml ${netConfDir}/storm.yaml;
	echo "cp ${netConfDir}/storm-rdma.yaml ${netConfDir}/storm.yaml";
	echo "Change storm configuration to infiniband";

	cp ${netConfDir}/zoo-ib.cfg ${netConfDir}/zoo.cfg;
	echo "cp ${netConfDir}/zoo-ib.cfg ${netConfDir}/zoo.cfg";
	echo "Change zookeeper configuration to infiniband";
fi

for i in {1..8}; do
	if [ "$1" == "eth" ]; then
		cp ${netConfDir}/storm.yaml ${netConfDir}/storm-temp.yaml;
		echo "storm.local.hostname: SN0${i}.eth" >> ${netConfDir}/storm-temp.yaml;
		scp -P 10022 ${netConfDir}/storm-temp.yaml sn0${i}:/home/team1/seokwoo/storm/conf/storm.yaml;
		rm ${netConfDir}/storm-temp.yaml;
	elif [ "$1" == "ib" ]; then
		cp ${netConfDir}/storm.yaml ${netConfDir}/storm-temp.yaml;
		echo "storm.local.hostname: SN0${i}" >> ${netConfDir}/storm-temp.yaml;
		scp -P 10022 ${netConfDir}/storm-temp.yaml sn0${i}:/home/team1/seokwoo/storm/conf/storm.yaml;
		rm ${netConfDir}/storm-temp.yaml;
	elif [ "$1" == "rdma" ]; then
		cp ${netConfDir}/storm.yaml ${netConfDir}/storm-temp.yaml;
		echo "storm.local.hostname: SN0${i}" >> ${netConfDir}/storm-temp.yaml;
		scp -P 10022 ${netConfDir}/storm-temp.yaml sn0${i}:/home/team1/seokwoo/storm/conf/storm.yaml;
		rm ${netConfDir}/storm-temp.yaml;
	fi
	scp -P 10022 ${netConfDir}/zoo.cfg sn0${i}:/home/team1/seokwoo/zookeeper/conf/zoo.cfg;
done
	
if [ "$1" == "eth" ]; then
	echo "storm.local.hostname: MN.eth" >> ${netConfDir}/storm.yaml;
elif [ "$1" == "ib" ]; then
	echo "storm.local.hostname: MN" >> ${netConfDir}/storm.yaml;
#elif [ "$1" == "rdma" ]; then
#	echo "storm.local.hostname: MN" >> ${netConfDir}/storm.yaml;
fi

cp ${netConfDir}/storm.yaml ${STORM_HOME}/conf;
cp ${netConfDir}/zoo.cfg ${ZOOKEEPER_HOME}/conf;
