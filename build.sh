#!/bin/sh

dir=`dirname $0`
java -Xmx512M -Xms128M -jar $dir/core/lib/ant-launcher.jar -lib $JAVA_HOME/lib -lib $dir/core/lib -q -f $dir/all/build.xml $*


