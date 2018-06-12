#!/bin/bash

descendent_pids() {
    pids=$(pgrep -P ${1})
    for pid in ${pids}; do
        arr+=(${pid})
        ppid=${pid}
        descendent_pids ${ppid}
    done
}

arr=(${1})
descendent_pids ${1}

for lpid in ${arr[@]}
do
        kill ${lpid}
done

sleep 1

for lpid in ${arr[@]}
do
        kill -9 ${lpid}
done
