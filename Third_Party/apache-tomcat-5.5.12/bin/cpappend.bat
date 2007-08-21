rem ---------------------------------------------------------------------------
rem Append to CLASSPATH
rem
rem $Id: cpappend.bat,v 1.3 2002/08/04 18:19:43 patrickl Exp $
rem ---------------------------------------------------------------------------

rem Process the first argument
if ""%1"" == """" goto end
set CLASSPATH=%CLASSPATH%;%1
shift

rem Process the remaining arguments
:setArgs
if ""%1"" == """" goto doneSetArgs
set CLASSPATH=%CLASSPATH% %1
shift
goto setArgs
:doneSetArgs
:end
