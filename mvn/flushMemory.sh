#!/bin/bash

if ! [ $(id -u) = 0 ]; then
	echo "Not root! Reattempting as root..."
	sudo -n ${0}
	if [ $? = 1 ]; then
		echo "Root not available! Cannot flush memory. (To fix please add ${0} to sudoers file)"
	fi
else
	echo "Root! Flushing memory..."
	sh -c "sync; echo 3 > /proc/sys/vm/drop_caches"
fi
