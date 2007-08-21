RBNB-@__RBNB.VERSION__@ has been successfully installed to:

    $INSTALL_PATH


Usage
-----

To use this product, start the Web server by either:

    1) WINDOWS: using the shortcut provided on the desktop.
    2) LINUX/UNIX/MACOSX: by executing the script:

          $INSTALL_PATH/bin/Start_WebServer.sh

Once the Web server has been started, point your Web browser to:

    http://localhost

(You may need to tell your browser not to try the firewall for that address).
There you will find documentation and links that will allow you to start and
configure the RBNB DataTurbine server and applications.


Third Party Software
--------------------

Various third party packages are required by some applications delivered with
the RBNB system.  These include:

    1) Sun's Java Media Framework (JMF) is required to capture video, and
    2) Sun's Java Advanced Imaging API (JAI) is required to use the PNG
       plugin.

1) Sun Java Media Framework

The Sun Java Media Framework (JMF) is used by the video capture code to capture
video.  It is required to run the rbnbjcap.jar file.

You can download the Java Media Framework from http://java.sun.com.

Once you've unpacked this package, make sure the JMF JAR file is in your
CLASSPATH environment variable value or is in the extension directory of your
Java SDK or JRE installation (usually in JAVA_HOME/jre/lib/ext).

2) Sun Java Advanced Imaging API

The Sun Advanced Imaging API (JAI) is used by the PNG plugin code to display
PNG images.  It is required to run the PNG plugin.

You can download the Java Advanced Imaging API from http://java.sun.com.  The
installation for this package automatically installs it in either your
CLASSPATH or the extension directory of your SDK (JDK) or JRE installation.
