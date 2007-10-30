RBNB JavaBean to ActiveX Interface
Installation Notes

See the HTML documentation for a full description; this is a quick summary.

1) You will need a Microsoft compatible compiler, V 6.0 or later.  Run
       VCVARS32.bat.  If you don't have one, have someone who does execute
	   steps 1 and 2, and give you the results.  Then skip to step 3.
2) Run pack.bat.  It should output 5 DLLs in the current directory.
3) Run:
	reg.bat [path to your JRE]
	
	Note that the JRE should be the PUBLIC jre, not the one hidden inside a 
	JDK installation.
	
You should now be able to see the controls with any ActiveX control client or
COM browser.