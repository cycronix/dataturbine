#!/bin/bash
# -----------------------------------------------------------------------------
# JavaEnv.sh - Determines JAVA_HOME, then executes CATALINA startup.
#
# 03/02/2002  WHF  Created
#

jeTemp=`which javac 2>/dev/null`
if ! [ ${jeTemp} ]; then
	# Failure, find jre:
	jeTemp=`which java 2>/dev/null`
	if ! [ ${jeTemp} ]; then
		echo "Could not find an instance of java in the path."
		exit 1
	fi
fi
JAVA_HOME=`dirname ${jeTemp}`/..
chmod a+rx *
./startup.sh



