call mvn clean install -DskipTests
call copy target\saiku-reporting-backend-trunk-SNAPSHOT.jar c:\TargetPlatforms\biserver-ce-4.8.0-stable\pentaho-solutions\system\saiku-reporting\lib\ /Y
