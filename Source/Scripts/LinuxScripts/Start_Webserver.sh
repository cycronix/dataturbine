#!/bin/bash
# -----------------------------------------------------------------------------
# check_JAVA_HOME - starts Catalina
#
# 06/10/2004  INB  Eliminated IBM requirement.
# 03/07/2002  WEA  Created
#

# Check to see if JAVA_HOME is set
if [ ! ${JAVA_HOME} ]; then 
    echo "Start_Webserver: JAVA_HOME not defined."

    declare -x JAVA_HOME=`which java 2>/dev/null`
    if [ ! ${JAVA_HOME} ]; then
	echo "Start_Webserver: Could not find instance of java in path."
	exit 1
    else
	JAVA_HOME=`dirname ${JAVA_HOME}`/..
	echo Start_Webserver: setting JAVA_HOME to $JAVA_HOME
    fi
fi

# Check to see if JAVA_HOME points to an IBM JRE
#foo=`echo $JAVA_HOME | grep IBM`
#if [ ! ${foo} ]; then
#    echo Start_Webserver: '************** WARNING **************'
#    echo Start_Webserver: Use of an IBM Java JRE is recommended.
#    echo Start_Webserver: You might NOT be using the IBM JRE.
#    echo Start_Webserver: your JAVA_HOME is $JAVA_HOME
#    echo Start_Webserver: '*************************************'
#fi 

# Start Catalina
export CATALINA_OPTS="-mx128m"
cd ../apache-tomcat-*/bin
chmod a+rx *.sh
./catalina.sh start "$@"



