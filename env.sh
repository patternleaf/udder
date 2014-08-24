# Conditionally set up paths for Java and Maven. Do nothing if the paths are
# already set. This script was tested on OS X 10.9.3 Mavericks. It will certainly
# require tailoring for Linux.
#
# Run this script in your current shell, like ". env.sh" or "source env.sh".

echo "Configuring paths for the Apache Maven build manager."
echo


export PATH=${M2}:${PATH}

if [ -z "$JAVA_HOME" ]
then
    # Automatically locate java_home (works on OS X, untested on Linux)
    export JAVA_HOME="`/usr/libexec/java_home`"
fi
echo "JAVA_HOME=${JAVA_HOME}"
echo

echo "Your Java runtime:"
which java
java -version
echo

echo "Your Java compiler:"
which javac
javac -version
echo

if [ -z "$M2_HOME" ]
then
    export M2_HOME=`pwd`/apache-maven/apache-maven-3.2.3
fi
echo "M2_HOME=${M2_HOME}"

if [ -z "$M2" ]
then
    export M2=${M2_HOME}/bin
fi
echo "M2=${M2}"

# Conditionally add to path.
if [[ ":$PATH:" != *":${M2}:"* ]]
then
    PATH=${PATH}:${M2}
fi

echo "Your Maven build manager:" 
which mvn
mvn --version
echo

