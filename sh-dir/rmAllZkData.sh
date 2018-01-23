#!/bin/bash

for slave in $(cat /home/team1/seokwoo/sh-dir/nodes.txt); do
	echo "remove zookeeper data in ${slave}";
	ssh -p 10022 team1@${slave} "rm -rf /home/team1/seokwoo/zookeeper/data/version-2; rm /home/team1/seokwoo/zookeeper/logs/zookeeper.out";
done

echo "remove zookeeper data in MN";
rm -rf /home/team1/seokwoo/zookeeper/data/version-2;
rm /home/team1/seokwoo/zookeeper/logs/zookeeper.out;
