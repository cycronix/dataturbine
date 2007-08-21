README for RBNB Source
----------------------

This file contains instructions for using the RBNB source code to build new
releases of the RBNB system (WebTurbine and DataTurbine).


Structure
---------

The RBNB source zip archive unpacks into a directory hierarchy that looks like
the following:

RBNB
    <version>
        Source
            {source files and directories}
        Build
            {intermediate files, including class files, JAR files, etc.}
        Distribute
            {distributable files}

Where <version> is the version of the RBNB software.

The Build and Distribute directories are not actually part of the source
archive.  Instead they are created as needed when building the RBNB system.


Requirements
------------

To be able to compile the RBNB system, you will need the following:

   1) Java SDK (release 1.4 or later)
   2) Apache Ant (release 1.6.1 or later),
   3) Sun Java Media Framework (release 2.1 or later)
   4) Sun Java Advanced Imaging API (release 1.1.2-beta or later)

1) Sun Java SDK

The Java SDK is used to compile the RBNB source code.  While the DataTurbine
server, itself, is Java 1.1 compatible, compilation requires a newer SDK, 
1.4 or later.  We suggest using a Sun SDK for compilation.

You can download the Sun SDK from http://java.sun.com.

You may need to perform the following after installing the SDK:

   a) Create an environment variable JAVA_HOME and set its value equal to the
      Java installation directory, and
   b) Add the SDK bin directory  to your path (JAVA_HOME/bin).

2) Apache Ant

The Apache Ant package is used to control the compilation and installer build
process.  The installation zip file for this package can be found in
RBNB/<version>/Source/Third_Party.

Once you've unpacked this package, perform the following:

   a) Create an environment variable ANT_HOME and set its value equal to the
      Apache Ant installation directory, and
   b) Add the Apache Ant bin directory to your path (ANT_HOME/bin).

3) Sun Java Media Framework

The Sun Java Media Framework (JMF) is used by the video capture code to capture
video.  It is required to compile the video code.

You can download the Java Media Framework from http://java.sun.com.

Once you've unpacked this package, perform the following steps:

   a) Open the file RBNB/<version>/Source/Applications/Video/build.xml in an
      editor,
   b) Find the line that says:

<classpath path="${rbnb.basedir}/../LicensedSoftware/JMF-2.1.1e/lib/jmf.jar" />

   c) Replace the ${rbnb.basedir}/../LicensedSoftware/JMF-2.1.1e part with
      the JMF installation directory.

Alternatively, you can ensure that the JMF JAR file is in your CLASSPATH
environment variable value or is in the extension directory of your Java SDK
installation (usually in JAVA_HOME/jre/lib/ext).

4) Sun Java Advanced Imaging API

The Sun Advanced Imaging API (JAI) is used by the PNG plugin code to display
PNG images.  It is required to compile the PNG plugin.

You can download the Java Advanced Imaging API from http://java.sun.com.

Once you've unpacked this package, perform the following steps:

   a) Open the file RBNB/<version>/Source/Applications/PlugIns/build.xml in an
      editor,
   b) Find the lines that say:

<classpath path="${rbnb.basedir}/../LicensedSoftware/jai-1_1_2-beta/lib/jai_core.jar" />
<classpath path="${rbnb.basedir}/../LicensedSoftware/jai-1_1_2-beta/lib/jai_codec.jar" />

   c) Replace the ${rbnb.basedir}/../LicensedSoftware/jai-1_1_2-beta part with
      the JAI installation directory.

Alternatively, you can ensure that the JAI JAR files are in your CLASSPATH
environment variable value or is in the extension directory of your Java SDK
installation (usually in JAVA_HOME/jre/lib/ext).


System Dependent Notes
----------------------

Windows and Unix
----------------

o) The Java SDK installations for these two systems tend to follow the standard
   directory hierarchy and file naming system set up by Sun Microsystems.  Both
   systems use the same ProGuard obfuscation file called
   standard-proguard.conf.


Macintosh (MacOSX)
------------------

o) The Java and JMF installations on MacOSX do not follow the standards used by
   other systems.  You will need to set the JAVA_HOME and JMF_HOME variables to
   values appropriate to your system.

o) Due to the differences between the MacOSX Java installation and the standard
   followed by most other systems, there is a separate ProGuard obfuscation
   configuration file for MacOSX.  The file is called macosx-proguard.conf.


Building the RBNB System
------------------------

To build the RBNB system, simply perform the following steps:

   1) Change to the RBNB/<version>/Source directory,
   2) Run ant.


Example for Linux:

   % cd RBNB/V2.4/Source
   % ant

This runs the default distribute-izpack target.  The result will be an
installer JAR file in the RBNB/<version>/Distribute directory (along with a
number of intermediate files in the RBNB/<version>/Build directory).


Ant Targets
-----------

In addition to the default distribute-izpack target, there are a number of
other useful targets, including:

   clean - deletes the Build and Distribute directories.

   compile-dataturbine - compiles just the RBNB DataTurbine server and SAPI.

   library-dataturbine - builds the RBNB DataTurbine server and API library.
			 Depends on the compile-dataturbine target.

   obfuscate-dataturbine - obfuscates the DataTurbine/API rbnb.jar file to
			   protect it against hackers using a decompiler by
			   renaming internal methods and variables to
			   meaningless values.  Depends on the
			   library-dataturbine target.

   documentation - Builds the SAPI JavaDoc documentation and copies all of the
		   documentation to the build directory.  Depends on the
		   obfuscate-dataturbine target.

   compile - compiles all of the system.  Depends on the documentation target.

   library - compiles and builds all of the various JAR files.  Depends on the
	     documentation target.

   distribute - builds a distributable system.  Depends on the library target.

The first three targets can also be used in lower-level directories to build
individual pieces of the RBNB system.
