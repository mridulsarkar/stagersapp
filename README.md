# Steps to have this spring-boot app running in your local Mac
**Home-brew**:-
Install:
$ ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

Update:
$ brew update

**Node.js and Npm**:-
Install
$ brew install node

**MongoDB**:-
Install:
$ brew tap mongodb/brew
$ brew update
$ brew install mongodb-community
 
Run standalone:
$ brew services start mongodb-community
$ brew services stop mongodb-community

Run as Replica-set (https://www.npmjs.com/package/run-rs):
$ npm install run-rs-g
$ run-rs --mongod

**Mongo CLT**:-
$ mongosh

**Maven**:-
Install - Manually:
1. Download latest apache-maven-X.X.X-bin.tar.gz file from https://maven.apache.org/download.cgi
2. Untar and Unzip
    $ tar -xvzf apache-maven-X.X.X-bin.tar.gz

Install - Using Brew:
$ brew install maven

Set Maven and JAVA Home
$ vi ~/.bash_profile
   	export M2_HOME="~/projects/apache-maven-3.8.6"
	export JAVA_HOME=$(/usr/libexec/java_home)
   	PATH="${M2_HOME}/bin:${JAVA_HOME}/bin:${PATH}"
   	export PATH

**JDK**:-
Install - Manually:
1. Download from https://jdk.java.net
2. Move downloaded bundle to directory /Library/Java/JavaVirtualMachines
3. Extract the bundle
    $ sudo tar -xzf openjdk-17.0.5_osx-x64_bin.tar.gz

** Compile and Install Stagers App**:-
$ git clone https://github.com/mridulsarkar/stagersapp
$ mvn compile 
$ mvn install
$ mvn clean install

** Run Stages App**:-
$ mvn spring-boot:run
