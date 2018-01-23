#!/bin/bash

SHDIR=`cat /home/team1/seokwoo/sh-dir/shdir.txt`
_PIDFILE="${SHDIR}/supervisor.pid"
_PID=`cat "${_PIDFILE}"`

echo "Storm Supervisor (pid=${_PID}) is stopping..."

kill -15 $_PID

rm "${_PIDFILE}"
