call mvn clean install -DskipTests
call copy target\saiku-adhoc-core-trunk-SNAPSHOT.jar c:\TargetPlatforms\biserver-saiku-4.5-dev\pentaho-solutions\system\saiku-adhoc\lib\ /Y
call c:\TargetPlatforms\biserver-ce-4.5-stable\start-pentaho-debug.bat
