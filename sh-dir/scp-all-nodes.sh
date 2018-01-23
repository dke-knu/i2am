#!/bin/bash

if [ $# -lt 1 ]; then
	echo "need source path & destination path";
fi

for slave in 1 2 3 4 5 6 7 8; do
	echo "SN0${slave}";
	scp -r -P 10022 $1 SN0${slave}.ib:$2;
done
