August 2011
John Wilson, Erigo Technologies LLC

Steps taken to update the version of Tomcat distributed with RBNB from ver 6.0.18 to ver 7.0.19
-----------------------------------------------------------------------------------------------

1. Download the “Core” Tomcat package in zip format from http://apache.tradebit.com/pub/tomcat/tomcat-7/v7.0.19/bin/apache-tomcat-7.0.19.zip and save it in Third_Party directory

2. Download the pgp signature for this zip file from http://www.apache.org/dist/tomcat/tomcat-7/v7.0.19/bin/apache-tomcat-7.0.19.zip.asc and also save it in Third_Party

3. Verify the downloaded Tomcat zip file; I used using Gpg4win as follows:
    
    in a DOS window, cd to the Third_Party directory and run:
    
    gpg --verify apache-tomcat-7.0.19.zip.asc apache-tomcat-7.0.19.zip
    
    Expected response something like the following:
    
    gpg: Signature made 07/14/11 04:02:21 Eastern Daylight Time using RSA key ID 2F6059E7
    gpg: Good signature from "Mark E D Thomas <markt@apache.org>"
    gpg: WARNING: This key is not certified with a trusted signature!
    gpg:          There is no indication that the signature belongs to the owner.
    Primary key fingerprint: A9C5 DF4D 22E9 9998 D987  5A51 10C0 1C5A 2F60 59E7
    
4. Delete apache-tomcat-7.0.19.zip.asc from the Third_Party directory

5. Delete the older Apache Tomcat zip file from the Third_Party directory (apache-tomcat-6.0.18.zip)

6. Edit Source\definitions.xml:
a) changed the value of rbnb.tomcatver from "apache-tomcat-6.0.18" to "apache-tomcat-7.0.19"
b) changed the value of rbnb.javaver from “1.4” to “1.5”

7. Edit Source\IzPack\distribute.xml; changed the value of TOMCATVER from "apache-tomcat-6.0.18" to "apache-tomcat-7.0.19"

8. We will keep the default version of apache-tomcat-7.0.19/bin/setclasspath.bat; to do this:
a) comment out the line to copy in a new setclasspath.bat which is in Source/WebTurbine/WebApplications/build.xml
b) delete Source\WebTurbine\WebServer\bin\setclasspath.bat

9. We will use a slightly tweaked version of conf/server.xml and conf/web.xml from Apache Tomcat 7; to do this:
a) Delete the currently used custom version of server.xml located at Source\WebTurbine\WebServer\conf\server.xml
b) Copy Third_Party/apache-tomcat-7.0.19.zip to a temporary location and extract all files
c) Copy server.xml and web.xml from the extracted apache-tomcat-7.0.19\conf to Source\WebTurbine\WebServer\conf
d) Edit Source\WebTurbine\WebServer\conf\server.xml as follows:
                                
    Change the default port to 80 from 8080 and the default SSL port to 443 from 8443.  Find the section of the file
    found below and make 3 changes total.
    
    <Connector port="80" protocol="HTTP/1.1" 
               connectionTimeout="20000" 
               redirectPort="443" />
    <!-- A "Connector" using the shared thread pool-->
    <!--
    <Connector executor="tomcatThreadPool"
               port="8080" protocol="HTTP/1.1" 
               connectionTimeout="20000" 
               redirectPort="8443" />
    -->           
    <!-- Define a SSL HTTP/1.1 Connector on port 8443
         This connector uses the JSSE configuration, when using APR, the 
         connector should be using the OpenSSL style configuration
         described in the APR documentation -->
    <!--
    <Connector port="443" protocol="HTTP/1.1" SSLEnabled="true"
               maxThreads="150" scheme="https" secure="true"
               clientAuth="false" sslProtocol="TLS" />
    -->
    
e) Edit Source\WebTurbine\WebServer\conf\web.xml as follows (this turns directory listings on):

    You will see a section like the following:
    
    <init-param>
        <param-name>listings</param-name>
        <param-value>false</param-value>
    </init-param>

    Change the false to true.

10. Do a clean build; install; test.  Some items to test:
a) Launch applications through the WebTurbine interface (server, admin, rbnbSource, rbnbPlot, PNGPlugIn)
b) Try fetching data through a browser with various munges; get both raw data and also plot data using the PNGPlugIn
c) Use HttpMonitor to put data into the server
d) Launch TimeDrive; in browser, request image data from (c) above through TimeDrive.
e) Run through MATLAB regression suite.
