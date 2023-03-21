QuamtumSpark Service API

This is a Service layer of a QuamtumSpark application providing a REST API model.

Instructions for local setup
	Required:
		Java 8
		Spring too suit V 4 or Any Java IDE 
		maven
		Apache Tomcat 9

	Step 1: Clone the code from feaure/april branch
	Step 2: Import the code to your Favourite IDE (Or)
	Step 3: Goto the "QuantumSparkAPI" directory  open command prompt and run "mvn install"
	Step 4: Once maven build completed plese navigate to "target" Directory
	Step 5: copy "QuantumSparkServiceAPI.war" to your webserver "webapps" directory and restart the webserver
	Step 6: Once webserver is started please enter below url in browser or postman
					
					GET : localhost:<WebServerPort>/QuantumSparkServiceAPI/api/v1/health 	[or]

					GET : localhost:<WebServerPort>/QuantumSparkServiceAPI/actuator/health
	
	Step 7: If you see reponse on your web page service is deployed properly.
					
					Swagger File :- 
					GET: localhost:<WebServerPort>/QuantumSparkServiceAPI/v2/api-docs
					GET: localhost:<WebServerPort>/QuantumSparkServiceAPI/swagger-ui.html
	


RESTful URLs

@QS Team