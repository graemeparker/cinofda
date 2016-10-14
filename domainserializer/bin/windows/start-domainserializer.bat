@ECHO OFF

SET args=
:Loop
IF "%1"=="" GOTO Continue
IF "%1"=="-1" (
    echo RunOnceMode is enabled
    SET "args=%args% -DrunOnceMode=true"
    SHIFT
    GOTO Loop
)
IF "%1"=="-ec" (
    echo Forcing CreativeEligibilityUpdater.enabled=true
    SET "args=%args% -DCreativeEligibilityUpdater.enabled=true"
    SHIFT
    GOTO Loop
)
IF "%1"=="-a" (
    echo Only serializing AdserverDomainCache
    SET "args=%args% -DomitDomainCache=true -DomitDataCollectorDomainCache=true"
    SHIFT
    GOTO Loop
)
IF "%1"=="-d" (
    echo Only serializing DomainCache
    SET "args=%args% -DomitAdserverDomainCache=true -DomitDataCollectorDomainCache=true"
    SHIFT
    GOTO Loop
)
IF "%1"=="-dc" (
    echo Only serializing DataCollectorDomainCache
    SET "args=%args% -DomitDomainCache=true -DomitAdserverDomainCache=true"
    SHIFT
    GOTO Loop
)
echo Unrecognized option: %1
SHIFT
GOTO Loop
:Continue

SET "args=%args% -Xmx2048m -DDomainSerializer.developerMode=true -Djava.util.logging.config.file=conf/logging.properties -Dlog4j.configuration=file:conf/log4j.properties -Dcom.sun.management.jmxremote.port=9103 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -cp %%classpath com.adfonic.tasks.DomainSerializer"

mvn compile exec:exec -Dexec.classpathScope=runtime -Dexec.executable="java" -Dexec.args="%args%"
