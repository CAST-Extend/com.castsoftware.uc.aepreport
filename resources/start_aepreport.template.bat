@echo off

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
SET CSSHOST=localhost
SET CSSPORT=2282
SET CSSDB=postgres
SET CSSUSER=operator
SET CSSPWD=CastAIP
:: if several schemas need to be parsed
::SET CENTRALSCHEMAS=schema1,schema2
SET CENTRALSCHEMAS=webgoat_demo_central

:: All applications
SET PROCESSAPPLICATION_FILTER=
:: Only Webgoat application (if there are several applications in the triplet)
::SET PROCESSAPPLICATION_FILTER=-processApplicationFilter "Webgoat"

::VERSIONS_LASTONE|VERSIONS_LASTTWO|VERSIONS_ALL
:: Last version
SET VERSION_FILTER=-versionFilter VERSIONS_LASTONE

::Environment (optional)
::SET ENVIRONMENT=-environment DEV 

::Output file prefix (default=AEPReport)
::SET OUTPUTFILEPREFIX=-outputFilePrefix AEPReport

::Output file prefix (if empty=current folder)
SET OUTPUTFOLDER=-outputFolder "C:/Temp"


:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

::IF %JAVA_HOME% == "" 
SET JAVA_HOME=C:\Program Files\Java\jre1.8.0_181

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
For /F "tokens=1* delims==" %%A IN (version.properties) DO (
    IF "%%A"=="version" set VERSION=%%B
)

::Check JRE Installation
IF NOT EXIST "%JAVA_HOME%\bin" GOTO JREPathNotSet

SET CMD="%JAVA_HOME%\bin\java" -jar AEPReport-%VERSION%.jar %OUTPUTFOLDER% %ENVIRONMENT% %OUTPUTFILEPREFIX% %PROCESSAPPLICATION_FILTER% %VERSION_FILTER% -dbHost %CSSHOST% -dbPort %CSSPORT% -dbDatabaseName %CSSDB% -dbUser %CSSUSER% -dbPassword %CSSPWD% -dbSchemas %CENTRALSCHEMAS%

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

ECHO running %CMD%
%CMD%
ECHO ========================================
SET RETURNCODE=%ERRORLEVEL%
IF NOT %RETURNCODE%==0 GOTO execError
GOTO end


:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:JREPathNotSet
ECHO The JRE Path %JAVA_HOME% is not correct
GOTO end

:execError
ECHO Error executing the command line
GOTO end

:end

PAUSE