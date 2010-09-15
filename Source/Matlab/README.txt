
Making SAPI calls from Matlab command prompt
============================================

The "Matlab" folder located in your DataTurbine installation contains a number
of ".m" files which provide examples of calling the DataTurbine simple API
(SAPI) from Matlab.  SAPI calls are easily made from Matlab, due in no small
part to Matlab's tight integration with Java.

Before you can make SAPI calls from Matlab, you will need to add the “rbnb.jar”
file to Matlab’s classpath.  To do this, startup Matlab and then at the Matlab
command prompt type “edit classpath.txt”.  Then add the full path to the
“rbnb.jar” file to this file.  As an example, here is a snippet from
“classpath.txt”:

	##
	## FILE: classpath.txt
	##
	## Generated from gen_classpath.pl
	##
	## Entries:
	##    o path_to_jarfile
	##    o [glnxa64,glnx86,sol2,sol64,unix,win32,win64,mac,maci,maci64]=path_to_jarfile
	##    o $matlabroot/path_to_jarfile
	##    o $jre_home/path_to_jarfile
	##
	C:\PROGRA~1\RBNB\V3.2B5\bin\rbnb.jar
	$matlabroot/java/patch
	$matlabroot/java/jar/util.jar
	$matlabroot/java/jar/widgets.jar
	mac=$matlabroot/java/jarext/aquaDecorations.jar
	etc
	etc
	etc

Notice that the full path to the desired DataTurbine "rbnb.jar" file
(“C:\PROGRA~1\RBNB\V3.2B5\bin\rbnb.jar”) has been added as the first non-comment
line in the file.

After editing "classpath.txt", save it, and then restart Matlab (so it will
pick up the new classpath.txt file).

To make sure everything is working, do the following after editing classpath.txt:

1. In a command/terminal window, startup a DataTurbine server.
2. Start Matlab
3. In Matlab, cd to the “Matlab” folder located in your DataTurbine installation.
4. From the Matlab command prompt, run “testrbnb.m”.  This executes the server
   test suite; various DataTurbine source/sink tests are executed.  You will
   see lots of activity in the DataTurbine server window, and at the Matlab
   command prompt you will see lines like:

   >> testrbnb
   PASS: open/close test
   PASS: testput1
   PASS: testput2
   PASS: testput3
   PASS: testmulti
   PASS: multiple ring buffer test
   etc
   etc
   etc

If you are able to run the "testrbnb.m" script as described above, then you are
set to try out SAPI calls on your own from the Matlab command prompt.  Look at
the “testput1.m” file to start.  Some convenience functions have been written,
such as rbnb_source.m (creates a source and opens an RBNB connection) and
rbnb_cmap.m (creates a channel map).  You can look at the testput1.m file
and try manually executing each command from the Matlab command prompt.

