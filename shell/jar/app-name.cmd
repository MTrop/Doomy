@echo off

REM ============================[ VARIABLES ]================================
SETLOCAL
SET JAVAEXE=
SET JAVAOPTS={{JAVA_OPTIONS}}
SET MAINCLASS={{MAIN_CLASSNAME}}
REM =========================================================================

where java > nul
if ERRORLEVEL == 0 (SET JAVAEXE=java)
if not %JAVAEXE%=="" goto _calljava

if not %JAVA_HOME%=="" (SET JAVAEXE=%JAVA_HOME%\bin)
if not %JAVAEXE%=="" goto _calljava

if not %JRE_HOME%=="" (SET JAVAEXE=%JRE_HOME%\bin)
if not %JAVAEXE%=="" goto _calljava

if not %JDK_HOME%=="" (SET JAVAEXE=%JDK_HOME%\bin)
if not %JAVAEXE%=="" goto _calljava

goto _end

:_calljava
PUSHD %~dp0
"%JAVAEXE%" -cp "%~dp0\jar\*" %JAVAOPTS% %MAINCLASS% %*
POPD

:_end
ENDLOCAL
