#!/bin/sh
export JD_CLI_OUT=out
rm -rf $JD_CLI_OUT
mkdir $JD_CLI_OUT
javac -d $JD_CLI_OUT -classpath $JD_CORE_PATH JdCli.java OptParse.java FileLister.java
cd $JD_CLI_OUT; jar cfe ../JdCli.jar JdCli com/github/hidenorly/jdcli/*.class; cd ..
