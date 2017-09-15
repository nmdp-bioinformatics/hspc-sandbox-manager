# HSPC Sandbox Manager

Welcome to the HSPC Sandbox Manager!  

# HSPC Sandbox

*Note:* If you are wanting to build and test SMART on FHIR Apps, it is recommended that you use the free cloud-hosted version of the HSPC Sandbox.

[HSPC Sandbox](https://sandbox.hspconsortium.org)

### How do I get set up? ###

#### Build and Deploy ####
    mvn clean install
    ./run_local.sh

#### Configuration ####

Various property files configure the sandbox manager:

 * src/main/resources/application.yml
 * src/main/resources/urlrewrite.xml
 * src/main/webapp/static/js/config/*

#### System Dependencies ####
When run locally, the sandbox manager has dependencies on the following systems/projects.  Each of these projects contains a "run_local" script in the root folder.
 * [HSPC Reference Auth](https://bitbucket.org/hspconsortium/reference-auth)
 * [HSPC Reference API](https://bitbucket.org/hspconsortium/reference-api) in DSTU2 or STU3 mode
 * [HSPC Reference Messaging](https://bitbucket.org/hspconsortium/reference-messaging)
 * [HSPC Account](https://bitbucket.org/hspconsortium/account)
 * [HSPC Patient Data Manager](https://bitbucket.org/hspconsortium/patient-data-manager)

### Where to go from here ###
https://healthservices.atlassian.net/wiki/display/HSPC/Healthcare+Services+Platform+Consortium