@echo off
setlocal
rem reg.bat:  Register RBNB beans.
rem 2007/10/29  WHF  Created.

if `%1` == `` (
	echo REG jre_path
	echo Copies ActiveX DLL's to the JRE\axbridge\bin directory and registers them.
	goto :EOF
)

set DEST=%~1\axbridge\bin
md "%DEST%" 2> NUL:

for %%I IN (*.dll) DO (
	copy "%%I" "%DEST%" > NUL:
	regsvr32 /s "%DEST%\%%I"
)
