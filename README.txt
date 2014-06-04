INSTALLATION INSTRUCTIONS:

1: Make sure MySQL server is running
	
2: Initialize the Database, by executing the sql scritpt mysql-setup.sql
   Do not forget to configure the correct connection inforation in META-INF/context.xml

3: Deploy the DB Anonymizer DBA GEi (eu.fiware.security.dbanonymizer-3.3-RELEASE.war) on a TomCat Server
   Two conf files:
		- /META-INF/context.xml: configuration of the database
		- /WEB-INF/beans.xml : internal configuration of DBA 

4: Execute "mvn dependency:copy-dependencies -DoutputDirectory=OUTPUT_DIR" to downoad all needed 
   library dependencies, and then copy all of them in the Tomcat webapp subfolder for DBA, in:
		- /WEB-INF/lib
