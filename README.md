# README #

Welcome to the HSPC Sandbox Manager!  

### How do I get set up? ###

#### Build and Deploy ####
    mvn clean install
    copy target/hspc-sandbox-manager.war to a web container

#### Configuration ####
HSPC Sandbox Manager is a tools for helping developers of SMART on FHIR applications test their launch their apps, manage their app's registration with the auth server and create/manage data in a FHIR server.

## Running in a local Tomcat container ##
Sandbox Manager uses url rewriting as part of it's implementation. To configure Tomcat to rewrite you need to: 

1. Copy sandbox-manager/src/main/resources/ebextensions/context.xml to <tomcat-root>/conf directory
   OR
   Add the rewrite valve to the individual host in <tomcat-root>/conf/server.xml

   Example:
   <Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="false">
       **<Valve className="org.apache.catalina.valves.rewrite.RewriteValve" />**
      ...
   </Host>

2. Replace src/main/webapp/WEB-INF/rewrite.config with src/main/webapp/WEB-INF/rewrite-local.config 

### Where to go from here ###
https://healthservices.atlassian.net/wiki/display/HSPC/Healthcare+Services+Platform+Consortium