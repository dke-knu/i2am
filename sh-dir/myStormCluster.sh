#!/bin/bash

if [ $# -lt 1 ] ; then
        echo "Usage: $0 [start | stop]";
        exit 0;
else

shdir=${SHDIR}

        if [ "$1" == "start" ]; then
                echo "[$HOSTNAME]: nimbus starting...";
                eval "$shdir/start-nimbus.sh;"

                for slave in $(cat /home/team1/seokwoo/sh-dir/nodes.txt); do
			#slave=SN0${i}.eth;
                        echo "[$slave]: supervisor starting...";
                        echo "[$slvae]: ssh $slave $shdir/start-supervisor.sh";
                        ssh -p 10022 $slave "$shdir/start-supervisor.sh";

                        echo "[$slave]: logviewer starting...";
                        echo "[$slave]: ssh $slave $shdir/start-logviewer.sh";
                        ssh -p 10022 $slave "$shdir/start-logviewer.sh";
                done

                echo "[$HOSTNAME]: ui and logviewer starting...";
                eval "$shdir/start-ui.sh; $shdir/start-logviewer.sh;"

        elif [ "$1" == "stop" ]; then
                echo "[$HOSTNAME]: ui and logviewer stopping...";
                eval "$shdir/stop-ui.sh; $shdir/stop-logviewer.sh;"

                for slave in $(cat /home/team1/seokwoo/sh-dir/nodes.txt); do
			#slave=SN0${i}.eth;
                        echo "[$slave]: supervisor stopping...";
                        echo "[$slave]: ssh $slave $shdir/stop-supervisor.sh";
                        ssh -p 10022 $slave "$shdir/stop-supervisor.sh";

                        echo "[$slave]: logviewer stopping...";
                        echo "[$slave]: ssh $slave $shdir/stop-logviewer.sh";
                        ssh -p 10022 $slave "$shdir/stop-logviewer.sh";
                done

                echo "[$HOSTNAME]: nimbus stopping...";
                eval "$shdir/stop-nimbus.sh;"

        fi
fi
