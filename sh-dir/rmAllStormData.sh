#!/bin/bash

for slave in $(cat /home/team1/seokwoo/sh-dir/nodes.txt); do
	echo "remove Storm data in ${slave}";
	ssh -p 10022 team1@${slave} "rm -rf /home/team1/seokwoo/storm/storm-local; rm -rf /home/team1/seokwoo/storm/logs/*";
done

echo "remove Storm data in MN";
rm -rf /home/team1/seokwoo/storm/storm-local;
rm -rf /home/team1/seokwoo/storm/logs/*;
