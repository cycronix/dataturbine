@echo off
setlocal
rem pack.bat  Package RBNB SAPI beans.
rem 2007/10/29  WHF  Created.

rem Set to the location of your RBNB.  Below should be correct if running
rem  from a standard distribution.
set RBNB_PATH=..\..\..\Build\Lib\rbnb.jar
rem According to the documentation, this step shouldn't be necessary, but it is.
copy %RBNB_PATH% .
rem Set to the full path of Java's packager.exe: 
set PACK_PATH=%JAVA_HOME%\bin\packager.exe

rem The CLSID's (class ids) are specified to allow portability between computers
rem   of documents that include the controls.

%PACK_PATH% -clsid {B88FF6C7-3CAB-45C7-964D-CF338630D018} -out %CD% rbnb.jar com.rbnb.sapi.ChannelMap
%PACK_PATH% -clsid {E5AF5737-904F-49DE-90D8-F569448B42D7} -out %CD% rbnb.jar com.rbnb.sapi.PlugInChannelMap
%PACK_PATH% -clsid {F587F109-0448-4F19-8B3C-156023100225} -out %CD% rbnb.jar com.rbnb.sapi.Sink
%PACK_PATH% -clsid {302CC70E-212B-497A-98B7-6C1EB12146C2} -out %CD% rbnb.jar com.rbnb.sapi.Source
%PACK_PATH% -clsid {F0BA6AC7-461A-4A74-968F-634CCE412910} -out %CD% rbnb.jar com.rbnb.sapi.PlugIn
del rbnb.jar
