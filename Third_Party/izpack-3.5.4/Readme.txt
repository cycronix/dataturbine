[ IzPack 2.x-3.x - Readme ]

    > 1 - A quick introduction <
    
    IzPack 2 is a Java software installer builder released under the terms of
the GNU GPL version 2 of the licence, or any later version. It is based around
an installer compiler that uses XML files to describe your installation.
IzPack is totally independant from the Operating System which runs it. It is 
also very modular so that you can easely create and integrate your own panels
(installation steps).

    Making installers can be made through 2 ways :
- by using a GUI-based frontend that avoids you of typing the XML files and 
submitting them to the command-line based compiler
- by making the XML files yourself and submitting them to the command-line
based compiler (usefull to integrate IzPack with another building process or
development environment).

    The IzPack homepage is http://www.izforge.com/izpack/ . You can contact the
author : julien@izforge.com. A mailing-list is also available : 
izpack.ml@izforge.com (send a email with 'subscribe' as the subject to 
izpack.ml_request@izforge.com).

    You can also get the latest development version (Unstable branch) from the
CVS server. You can log in as 'anonymous'. The CVSROOT is :
:pserver:login@cvs.tuxfamily.org:/cvsroot/izpack2 where 'login' can be replaced
by your CVS login (ask one if you want to contribute) or 'anonymous'.

    > 2 - The documentation <
    
    A documentation is available in the doc folder. It was generated with 
LaTeX under GNU-Linux. You can print a book version by modifying the 
izpack-doc.tex file (change the class from report to book) that is located in
the src/doc folder (provided that you installed the source code). A makefile
is available to compile the documentation, see the Readme file of the folder
for more.

    > 3 - Licencing issues <
    
    The licences used by IzPack or the libraries it uses are available in the 
legal folder. IzPack is covered by the GNU GPL 2 and any derivative work based
on it must also be covered by this licence.

    Generating an installer for commercial products is strongly encouraged, but
distributing an installer using a panel that is not released under a free 
licence compatible with GNU GPL 2 is not permitted as the installer is covered
by this licence.

# vim: fileformat=dos 
