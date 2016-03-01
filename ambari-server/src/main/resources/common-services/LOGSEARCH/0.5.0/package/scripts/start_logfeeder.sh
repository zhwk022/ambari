#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


set -e
if [ $# -lt 4 ]; then
    echo "Error: Not enough parameters. Count=$# excpected 4 or more"
    echo "Usage: <logfeeder path> <logfile> <pid file> <java_home> [java_mem]"
    echo "Example: $0 /opt/logfeeder /var/log/logfeeder/logfeeder.log /var/run/logfeeder/logfeeder.pid /usr/jdk64/jdk1.8.0_45 -Xmx512m"
    exit 1
fi

#!/bin/bash
set -e

#path containing start.jar file e.g. /opt/solr/latest/server
export LOGFEEDER_PATH=$1

#Logfile e.g. /var/log/solr.log
export LOGFILE=$2

#pid file e.g. /var/run/solr.pid
export PID_FILE=$3

export JAVA_HOME=$4

export LOGFEEDER_JAVA_MEM=$5
if [ "$LOGFEEDER_JAVA_MEM" = "" ]; then
    export LOGFEEDER_JAVA_MEM="-Xmx512m"
fi

cd $LOGFEEDER_PATH
echo "Starting Logfeeder logfile=$LOGFILE, PID_FILE=$PID_FILE, JAVA_HOME=$JAVA_HOME, MEM=$LOGFEEDER_JAVA_MEM ..."	
./run.sh
