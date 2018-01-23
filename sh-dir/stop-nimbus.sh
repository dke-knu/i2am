#!/bin/bash

SHDIR=`cat /home/team1/seokwoo/sh-dir/shdir.txt`
_PIDFILE="${SHDIR}/nimbus.pid"
_PID=`cat "${_PIDFILE}"`

echo "Storm Nimbus (pid=${_PID}) is stopping..."
kill -15 $_PID
rm "${_PIDFILE}"
