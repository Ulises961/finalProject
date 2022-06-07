# Installation


## Database set-up

1. Download PostgreSQL 
2. Download DBeaver
3. In DBeaver create a connection to SQL Server DB
    * Go to Database
    * New Database Connection
    * Search for 'PostgreSQL'
    * Insert the username and password of an existing postgres user
    * Click Finish
4. Inside the 'Database Navigator' click on the 'Database'connection you created
    * Right-click on the database that you used to create your connection
    * Go to create
    * Click on database
    * Name the new database 'finalproject' (its the db that our java app uses)
    * Click on finish
5. Let's create the DB tables used by the java app
    * Right click on the newly created DB
    * SQL Editor
    * New SQL Script
    * Copy-Paste the content of finalproject.sql
    * Right click on the editor
    * Execute -> Execute SQL Script

## Environment setup

1. Install Java JDK 11 (we used it during development)
2. Install Eclipse latest version 
3. Go to Help
    * Install new software... -> Work with: "Latest Eclipse Simultaneous Release - https://download.eclipse.org/releases/latest"
    * In the tab below, expand the "Web, XML, Java EE and OSGi Enterprise Development" checkbox
    * Check the following elements:
	    - Eclipse Java EE Developer Tools
	    - Eclipse Java Web Developer Tools
	    - Eclipse Java Web Developer Tools - JavaScript Support
	    - Eclipse Web Developer Tools
	    - Eclipse Web JavaScript Developer Tools
	    - JST Server Adapters
	    - JST Server Adapters Extension (Apache Tomcat)
    * Click Next two times, then accept the licence and click Finish
4) Restart Eclipse
5) Go to Window -> Preferences -> Server -> Runtime Environments -> Add... -> Apache -> Apache Tomcat v10.0 -> click 'Create new a local server' -> Next
6) Click 'Download and install...', that should install the latest stable version (currently 10.0.13) -> Choose your favourite folder for Tomcat installation
7) Since now you can see your installed web servers in the Eclipse 'Server' tab, if it is not displayed by default, you can enable it by going to Window -> Show view -> Server

## Project setup

1. Download the project as zip from GitHub link "https://github.com/Ulises961/finalProject" and extract the content 
2. Create a new java project in Eclipse
3. Import the extracted content as 'filesystem' into the java project 
4. Right click on the project -> Build path -> Configure build path...
5. Go to Libraries -> Click on classpath -> Add External Jars... -> Add the file "servlet-api.jar" from lib directory inside the Apache Tomcat folder
6. Update Maven dependencies by Rigth click on the project -> maven -> update project. This opens a window where one can confirm the update of dependencies.

--- You're ready now to build your Web Application using Java and Tomcat! ---

By using Eclipse you can...
1. create Servlets: right click on project -> New -> Servlet
	(note that Eclipse auto-generated servlets import the old 'javax' package still, replace it with 'jakarta' to work properly)
2. create HTTP/JSP/CSS/JavaScript files: right click on project -> New -> HTTP/JSP/CSS/JavaScript file
3. run your web application: right click on project -> Run As -> Run on Server -> Select the Apache Tomcat server -> Run -> Go to your browser on URL "http://localhost:8080/JavaProjectName/"
