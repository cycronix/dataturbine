@echo off
@rem =====================================
@rem DOS Batch file to invoke the compiler
@rem =====================================

SET IZH=/home/SDP/RBNB/izpack-3.5.4
java -jar "%IZH%\lib\compiler.jar" -HOME "%IZH%" %1 %2 %3 %4 %5 %6 %7 %8 %9

@echo on
