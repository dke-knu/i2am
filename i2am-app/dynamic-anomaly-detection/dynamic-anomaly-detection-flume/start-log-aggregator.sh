#!/bin/bash
nohup bash -c './bin/flume-ng agent -n agent1 -f ./conf/kafka-sink.conf -c ./conf/flume-conf.properties.template' > aggregator.log 2>&1&
echo $! > aggregator.pid
