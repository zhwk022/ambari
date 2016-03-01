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

#path containing start.jar file e.g. /opt/solr/latest/server
export LOGSEARCH_PATH=$1

#Logfile e.g. /var/log/solr.log
export LOGFILE=$2

#pid file e.g. /var/run/solr.pid
export PID_FILE=$3

export JAVA_HOME=$4

export LOGSEARCH_JAVA_MEM=$5
if [ "$LOGSEARCH_JAVA_MEM" = "" ]; then
    export LOGSEARCH_JAVA_MEM="-Xmx1g"
fi

cd $LOGSEARCH_PATH
echo "Starting Logsearch..."	
./run.sh
