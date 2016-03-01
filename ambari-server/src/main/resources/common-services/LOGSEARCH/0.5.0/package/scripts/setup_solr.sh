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



#Path to install Solr to e.g. /opt/solr
SOLR_PATH=$1

#solr user e.g. solr
SOLR_USER=$2


echo "Starting Solr install"

getent passwd $SOLR_USER
if [ $? -eq 0 ]; then
    echo "the user exists, no need to create"
else
    echo "creating solr user"
    adduser $SOLR_USER
fi


#hadoop fs -test -d /user/$SOLR_USER
#if [ $? -eq 1 ]; then
#    echo "Creating user dir in HDFS"
#    sudo -u hdfs hdfs dfs -mkdir -p /user/$SOLR_USER
#    sudo -u hdfs hdfs dfs -chown $SOLR_USER /user/solr 
#fi
	

