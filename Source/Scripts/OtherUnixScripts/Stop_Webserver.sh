#!/bin/sh
# -----------------------------------------------------------------------------
# check_JAVA_HOME - stops Catalina


#
# 03/07/2002  WEA  Created
#

# Check to see if JAVA_HOME is set 
if [ ! ${JAVA_HOME} ]; then 
    echo "Stop_Webserver: JAVA_HOME not defined."

    declare -x JAVA_HOME=`which java 2>/dev/null`
    if [ ! ${JAVA_HOME} ]; then
	echo "Stop_Webserver: Could not find instance of java in path."
	exit 1
    else
	JAVA_HOME=`dirname ${JAVA_HOME}`/..
	echo Stop_Webserver: setting JAVA_HOME to $JAVA_HOME
    fi
fi

# Stop Catalina
cd ../apache-tomcat-*/bin
chmod a+rx *.sh
./catalina.sh stop "$@"




