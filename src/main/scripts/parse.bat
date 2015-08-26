@echo off
rem ===========================================================================
rem
rem  This script is used to run the Account Parser utility.
rem
rem  Usage:
rem     parse [-<options>] <csv-file>
rem
rem  Where
rem     <options> is one or more of the following:
rem       ? - ?????
rem       v - generate additional console output
rem       q - generate no console output
rem     <csv-file> is a CSV file containing account transactions obtained from
rem                the bank's web site 
rem
rem ===========================================================================
setlocal

REM The app's JAR file should be in the same directory as this script
set DIRNAME=%~dp0%
REM The app's directory is first in the classpath so that the properties file
REM and log4j2.xml file can be placed there to override the defaults.
set CLASSPATH=%DIRNAME%
set CLASSPATH=%CLASSPATH%;%DIRNAME%${project.artifactId}-${project.version}.jar

REM The third-party JARs should be in the lib subdirectory
set LIBDIR=%DIRNAME%lib
set CLASSPATH=%CLASSPATH%;%LIBDIR%\log4j-api.jar
set CLASSPATH=%CLASSPATH%;%LIBDIR%\log4j-core.jar

REM If JAVA_HOME is set, use that, otherwise hope that java is in the PATH
IF "%JAVA_HOME%" == "" (
  set JAVA=java
) ELSE (
  set JAVA=%JAVA_HOME%\bin\java
)

REM Run the app
"%JAVA%" -classpath %CLASSPATH% org.cafed00d.account.Parse %* 
