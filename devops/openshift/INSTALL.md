
# OPENSHIFT INSTALLATION GUIDE -- IT 2019-2

Installation of Web Application Intygstjänsten (IT) on OpenShift.

## 1 Updates since 2019-1 (release notes)

### 1.1 Database schema

Database schema doesn't need any updates.

### 1.1 Configuration of reference data

The main update is activation of the new reference data concept (master data for shared configurations). Refdata is provided as a JAR file and configured with the `REFDATA_URL` and `RESOURCES_FOLDER` parameters. Normally the default value of `RESOURCES_FOLDER` should be set to  `classpath:`. Three configuration updates is required in order to activate the new refdata:

1. Parameter `REFDATA_URL` shall be set to the actual location of the refdata JAR artefact.
2. Parameter `RESOURCES_FOLDER` or `-Dresources.folder=...` in `secret-env.sh` shall be set to `classpath:`. Though, it's recommended to remove this parameter from `secret-env.sh`. 
3. The old `resources.zip` must be removed in order to enable the `REFDATA_URL` setting. 

Latest builds of refdata can be downloaded from the Inera Nexus server. 

	https://build-inera.nordicmedtest.se/nexus/repository/releases/se/inera/intyg/refdata/refdata/1.0.0.<build-num>/refdata-1.0.0.<build-num>.jar

### 1.2 Other recommendations

The following (well known) observations might require an action:

* Currently IT is under-dimensioned resulting in too long response times, and thus a bad user experience. The deployment for IT must be assigned more CPU and Memory. At least 2x CPU Cores and 1x GB of Java Heap Memory.
* Client certificates are not issued for the FQDN in question
* Mutual TLS is not respected when terminating RIV-TA requests
* The current configuration file `certificate.properties` contains redundant settings. The goal is to remove all parameters from this file and only use parameters in `intygstjanst-configmap-envvar`. 


## 2 Pre-Installation Requirements

The following prerequisites and requirements must be satisfied in order for IT to install successfully.

### 2.1 Backing Service Dependencies

The application has the following external services: 

On premise (execution environment):

* MySQL
* ActiveMQ
* Redis Sentinel
* Redis Server

Provided elsewhere:

* Inera Service Platform (Tjänsteplattformen, NTjP)

For all backing services their actual addresses and user accounts have to be known prior to start the installation procedure.  

### 2.3 Integration / Firewall

IT communicates in/out with the Inera Service Platform and thus needs firewall rules for that access.

### 2.4 Certificates

IT requires certificates, keystores and truststores for communicating with NTjP. The operations provider is responsible for installing these certificates in the appropriate OpenShift "secret", see detailed instructions in the OpenShift section.

### 2.5 Message Queues

Two queues needs are required and depending on permissions those may be implicitly created by the application.

- `statistik.utlatande.queue` -- sends certificates to Statistiktjänsten (ST) 
- `internal.notification.queue` -- sends notifications to WebCert (WC)

### 2.5 Database

A database for the application must have been created.  It's recommended to use character set `utf8mb4` and case-sensitive collation. 

### 2.6 Access to Software Artifacts

Software artifacts are located at, and downloaded from:

* From Installing Client - [https://build-inera.nordicmedtest.se/nexus/repository/releases/se/inera/intyg/intygstjanst/intygstjanst/maven-metadata.xml](https://build-inera.nordicmedtest.se/nexus/repository/releases/se/inera/intyg/intygstjanst/intygstjanst/maven-metadata.xml)
* From OpenShift Cluster - docker.drift.inera.se/intyg/

### 2.7 Access to OpenShift Cluster

The OpenShift user account must have the right permissions to process, create, delete and replace objects. A VPN account and connection is required in order to access the OpenShift Cluster.

### 2.8 Client Software Tools

The installation client must have **git** and **oc** (OpenShift Client) installed and if a database schema migration is required then **java** (Java 8) and **tar** is required in order to execute the migration tool (liquibase runner).

Must have:

* git
* oc
* VPN Client (such as Cisco Any Connect) 

To run database migration tool:

* java
* tar

# 3 Installation Procedure

### 3.1 Installation Checklist

1. All Pre-Installation Requirements are fulfilled, se above
2. Check if a database migration is required
3. Ensure that the secrets `intygstjanst-env`, `intygstjanst-certifikat` and `intygstjanst-secret-envvar` are up to date
4. Ensure that the config maps `intygstjanst-config` and `intygstjanst-configmap-envvar` are up to date
5. Check that deployment works as expected 
6. Fine-tune CPU and memory settings for container and java process
7. Setup policies for number of replicas, auto-scaling and rolling upgrade strategy


### 3.2 Migrate Database Schema

Prior to any release that includes changes to the database schema, the operations provider must execute schema updates using the Liquibase runner tool provided in this section. 

_Please note: a complete database backup is recommended prior to run the database migration tool_

Replace `<version>` below with the actual application version.

Fetch the actual version of the tool, the example below runs `wget` to retrieve the package (tarball).

    > wget https://build-inera.nordicmedtest.se/nexus/repository/releases/se/inera/intyg/intygstjanst/intygstjanst-liquibase-runner/<version>/intygstjanst-liquibase-runner-<version>.tar


Download onto a computer having Java installed and network access to the database and execute the runner.

    > tar xvf intygstjanst-liquibase-runner-<version>.tar
    > cd intygstjanst-liquibase-runner-<version>
    > bin/intygstjanst-liquibase-runner --url=jdbc:mysql://<database-host>/<database-name> --username=<database_username> --password=<database_password> update


### 3.3 Get Source for Configuration


##### 3.3.1 Clone the repository

Clone repository and switch to the release branch specified in the release notes.
    
    > git clone https://github.com/sklintyg/intygstjanst.git
    > git checkout release/2019-2
    > cd devops/openshift
    
Note that we strongly recommend using a git account that has read-only (e.g. public) access to the repo.
    
##### 3.3.2 Log-in into the cluster

Use oc to login and select the actual project, e.g:

    > oc login https://path.to.cluster
    username: ******
    password: ******
    > oc project <name>

#### 3.3.3 Ensure an up to date deployment configuration

A template for the deployment can be dowloaded from [deploytemplate-webapp.yaml](https://github.com/sklintyg/tools/blob/develop/devops/openshift/deploytemplate-webapp.yaml). This needs to be updated regarding assigned computing resources, i.e. the requested and limited amount of CPU needs to be increased as well as the Java memory heap settings, see `JAVA_OPTS`.

Syntax to create or replace the template: 

	> oc [ create | replace ] -f deploytemplate-webapp.yaml

### 3.4 Update configuration placeholders

For security reasons, no secret properties or configuration may be checked into git. Thus, a number of placeholders needs to be replaced prior to creating or updating secrets and/or config maps.

Open _&lt;env>/secret-vars.yaml_ and assign corrrect values:

    ACTIVEMQ_BROKER_USERNAME: "<username>"
    ACTIVEMQ_BROKER_PASSWORD: "<password>"
    DATABASE_USERNAME: "<username>"
    DATABASE_PASSWORD: "<password>"
    NTJP_WS_CERTIFICATE_PASSWORD: "<password>"
    NTJP_WS_KEY_MANAGER_PASSWORD: "<password>"
    NTJP_WS_TRUSTSTORE_PASSWORD: "<password>"

Open _&lt;env>/configmap-vars.yaml_ and replace `<value>` with expected values. You may also need to update name of keystore/truststore files as well as their type (JKS or PKCS12)

    REFDATA_URL: "<url>"
    RESOURCES_FOLDER: "classpath:"
    NTJP_BASE_URL: "<url>"
    REDIS_SERVICE_HOST: "<hostname1[;hostname2;...]>"
    REDIS_SERVICE_PORT: "<port1[;port2;...]>"
    REDIS_SENTINEL_MASTER_NAME: "<name>"
    ACTIVEMQ_BROKER_URL: "<failover:(url1, url2, ...)>"
    ACTIVEMQ_DESTINATION_QUEUE_NAME: "statistik.utlatande.queue"
    INFRASTRUCTURE_DIRECTORY_LOGICALADDRESS: "<address>"
    REDIS_SENTINEL_MASTER_NAME: "<name>"
    TSBAS_SEND_CERTIFICATE_TO_RECIPIENT_REGISTERCERTIFICATE_VERSION: "v1"
    NTJP_WS_CERTIFICATE_FILE: "${certificate.folder}/<filename>"
    NTJP_WS_TRUSTSTORE_FILE: "${certificate.folder}/<filename>"
    NTJP_WS_CERTIFICATE_TYPE: "<JKS | PKCS12>"
    NTJP_WS_TRUSTSTORE_TYPE: "<JKS | PKCS12>"
   
Note: Parameters shall follow the Java naming convention when used as in the value field, e.g. the path to certificates indicated by the `CERTIFICATE_FOLDER` property and the truststore file might be defined like:
 
	NTJP_WS_TRUSTSTORE_FILE: "${certificate.folder}/truststore-ntjp.jks"
        
The _&lt;env>/config/recipients.json_ file might have to be updated with new recipients.
    
##### 3.4.1 Redis Sentinel Configuration

Redis sentinel requires at least three URL:s passed in order to work correctly. These are specified in the `REDIS_SERVICE_HOST` and `REDIS_SERVICE_PORT` parameters respectively:

    REDIS_SERVICE_HOST: "host1;host2;host3"
    REDIS_SERVICE_PORT: "26379;26379;26379"
    REDIS_SENTINEL_MASTER_NAME: "<name>"
    
### 3.5 Prepare Certificates

The `<env>` placeholder might be substituted with the actual name of the environment such as `stage` or `prod`.

Staging and Prod certificates are **never** committed to git. However, you may temporarily copy them to _&lt;env>/certifikat_ in order to install/update them. Typically, certificates have probably been installed separately. The important thing is that the deployment template **requires** a secret named: `intygstjanst-<env>-certifikat` to be available in the OpenShift project. It will be mounted to _/opt/intygstjanst[-<env>]/certifikat_ in the container file system.


### 2.6 Creating Config and Secrets

If you've finished updating the files above, it's now time to use **oc** to install them into OpenShift.
All commands must be executed from the same folder as this markdown file, i.e. _/intygstjanst/devops/openshift_ 

Note: To delete an existing ConfigMap or Secret use the following syntax:

	> oc delete [ configmap | secret ] <name>

##### 3.6.1 Create environment variables for Secret and ConfigMap
From YAML-files, their names are hard-coded into the respective file

    > oc create -f <env>/configmap-vars.yaml
    > oc create -f <env>/secret-vars.yaml
    
##### 3.6.2 Create Secret and ConfigMap
Creates config map and secret from the contents of the _&lt;env>/env_ and _&lt;env>/config_ folders:

    > oc create configmap intygstjanst-<env>-config --from-file=<env>/config/
    > oc create secret generic intygstjanst-<env>-env --from-file=<env>/env/ --type=Opaque
    
##### 2.6.3 Create Secret with Certificates

If this hasn't been done previously, you may **temporarily** copy keystores into the _&lt;env>/certifikat_ folder and then install them into OpenShift using this command:

    > oc create secret generic intygstjanst-<env>-certifikat --from-file=<env>/certifikat/ --type=Opaque

### 3.7 Deploy
We're all set for deploying the application. As stated in the pre-reqs, the "deploytemplate-webapp" must be installed in the OpenShift project.

**Note 1** You need to reference the correct version of the docker image from the registry!

**Note 2** Please specify the `DATABASE_NAME` actual MySQL database. Default is **intygstjanst**.

**Note 3** Assign appropriate computing resources to the pods.

Create a deployment:

    > oc process deploytemplate-webapp \
        -p APP_NAME=intygstjanst \
        -p IMAGE=docker.drift.inera.se/intyg/intygstjanst-test:<version> \
        -p STAGE=<env> 
        -p DATABASE_NAME=intyg \
        -p HEALTH_URI=/inera-certificate/services \
        -o yaml | oc apply -f -
        
        
Alternatively, it's possible to use the deploytemplate-webapp file locally:

    > oc process -f deploytemplate-webapp.yaml -p APP_NAME=intygstjanst ...

##### 3.7.1 Computing resources
IT requires a lot of CPU, and especially for XML marshalling of certificates which also consumes significant amounts of Java heap memory.

Minimum requirements are:

1. 2x CPU Cores
2. 2x GB RAM
3. 1x GB Java Heap Size (JAVA_OPTS=-Xmx1G)

### 3.8 Verify
The pod(s) running intygstjanst should become available within a few minutes use **oc** or Web Console to checkout the logs for progress:

	> oc logs dc/intygstjanst

### 3.9 Routes
IT should _only_ be accessible from inside of the OpenShift project using its _service_ name (e.g. http://intygstjanst:8080) and from Nationella tjänsteplattformen, i.e. take care when setting up an OpenShift routes so the IT service isn't publicly accessible from the Internet.

The security measures based on mutual TLS and PKI should nevertheless stop any attempts from unsolicited callers.
