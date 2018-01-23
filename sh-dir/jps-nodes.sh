#!/bin/bash

echo "hostname: $HOSTNAME";
jps;

shdir=/home/team1/seokwoo/sh-dir
catslaves=$(cat ${shdir}/nodes.txt)
for slave in $catslaves; do
    echo "hostname: $slave";
    ssh -p 10022 $slave "jps";
done
