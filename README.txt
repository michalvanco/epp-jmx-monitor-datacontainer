JMX monitor tool for DataContainer size (used for JBoss EPP/GateIn)
-------------------------------------------------------------------
Author: Michal Vanco, mvanco@redhat.com

Tool is monitoring status (size) of DataContainer which aggregates all caches in portal, it sends requests to JMX regularly (thanks to quartz scheduler) and stores the size of DataContainer with current sessions count to a specified file.

This tool is based on Jolokia (remote JMX with JSON over HTTP), more details at http://jolokia.org/
It is also using quatrz for schedulling (currently there is a trigger to send request every minute).

MBean in exo group is used:
exo:cache-type=JCR_CACHE,jmx-resource=DataContainer,portal=\"portal\",repository=\"repository\",workspace=\"portal-system\"
and "getNumberOfNodes" operation is called.

Number of current sessions is read from MBean "jboss.web:host=localhost,path=/portal,type=Manager", attribute ActiveSessions.

Result is stored in csv file which can be set by system property "jmx.datacontainer.monitor.file".

To use this tool, simply build this project with 

mvn clean package

and deploy jmx-cache-monitor-tool-0.0.1-SNAPSHOT.war to your portal deploy folder.
You have to deploy also the jolokia*.war archive (jolokia agent), this can be downloaded from http://www.jolokia.org/download.html

Start your portal in common way (or use -Djmx.datacontainer.monitor.file=... if you want to change path of monitor file).

Quartz scheduler is initialized automatically at startup and data are written to file every minute (can be changed at src/main/resources/quartz.xml at <trigger> element).

Sample content of file is:

JMX stats,Date;No.of sessions;DataContainer-size
Thu May 31 01:45:11 CEST 2012;1;7765
Thu May 31 01:45:32 CEST 2012;5;6611

First collumn represents date, second is number of sessions, thirt is size of DataContainer.

---------------------------------------------------------------------

Feel free to re-use/update the code for your needs.
If you have any question, mail me at mvanco@redhat.com

Important:
-check your jmx-console if MBean names, methods and attributes are the same.
