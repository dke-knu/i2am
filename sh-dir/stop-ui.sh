#!/bin/bash

SHDIR=`cat /home/team1/seokwoo/sh-dir/shdir.txt`
_PIDFILE="${SHDIR}/ui.pid"
_PID=`cat "${_PIDFILE}"`

echo "Storm UI (pid=${_PID}) is stopping..."
kill -15 $_PID
rm "${_PIDFILE}"
