# 1. INSTALLATION INSTRUCTIONS
The first part deals with traditional actions and pre-requisites for a successful installation.

### 1.1 Service dependencies
The application has the following external service dependencies, all of which are provided by the operations provider:

* MySQL (provided)
* ActiveMQ (provided)
* Redis Sentinel (provided)
* Redis Server (provided)

### 1.2 Liquibase

Prior to any release that includes changes to the database schema, the operations provider must execute schema updates using the Liquibase runner application provided in this section.

Replace \<version\> below with the actual version.

    https://build-inera.nordicmedtest.se/nexus/repository/releases/se/inera/intyg/intygstjanst/intygstjanst-liquibase-runner/3.6.0.431/intygstjanst-liquibase-runner-3.6.0.431.tar

TODO Path to Intyg Nexus:

    intygstjanst-liquibase-runner/<version>/intygstjanst-liquibase-runner-<version>.tar

Download onto a computer having Java installed and network access to the database and execute the runner.

    tar xzf intygstjanst-liquibase-runner-<version>.tar
    cd intygstjanst-liquibase-runner-<version>.tar
    ./bin/liquibase-runner --url=jdbc:mysql://DATABASEHOST/intyg --username=<database_username> --password=<database_password> update

### 1.3 Integration / Firewall

Intygstjänsten communicates in/out with the Nationella Tjänsteplattformen and thus needs firewall rules for that access.

### 1.4 Certificates

Intygstjänsten needs certificates, keystores and truststores for communicating over Tjänsteplattformen. The operations provider is responsible for installing these certificates in the appropriate OpenShift "secret", see detailed instructions in the OpenShift section.

### 1.5 Queues

Two queues needs to be set up for Intygstjänsten (or ActiveMQ will create them automatically?)

- stage.statistik.utlatande.queue
- stage.internal.notification.queue

# 2. Installation on OPENSHIFT


### 2.1 Pre-req

- A git client must be available
- "oc" must be installed locally and available on your PATH.
- VPN connected to the appropriate network.

(1.) Clone the github repo and switch to the release branch specified in the release notes, e.g:

    
    > git clone https://github.com/sklintyg/intygstjanst.git
    > git checkout release/2018-4
    > cd devops/openshift
    
Note that we strongly recommend using a git account that has read-only (e.g. public) access to the repo.
    
(2.) Log-in into the cluster with oc and select the correct project, e.g:

    > oc login https://path.to.cluster
    username: ******
    password: ******
    > oc project stage

(3.) Make sure the latest deployment template is installed into the cluster, see https://github.com/sklintyg/tools/blob/develop/devops/openshift/deploytemplate-webapp.yaml

### 2.2 Update placeholders

For security reasons, no secret properties or configuration may be checked into git. Thus, a number of placeholders needs to be replaced prior to creating or updating secrets and/or config maps.

Open _stage/secret-vars.yaml_ and replace \<replaceme\> with expected values:

    ACTIVEMQ_BROKER_USERNAME: <replaceme>
    ACTIVEMQ_BROKER_PASSWORD: <replaceme>
    NTJP_WS_CERTIFICATE_PASSWORD: <replaceme>
    NTJP_WS_KEY_MANAGER_PASSWORD: <replaceme>
    NTJP_WS_TRUSTSTORE_PASSWORD: <replaceme>

Open _stage/configmap-vars.yaml_ and replace \<replaceme\> with expected values. You may also need to update name of keystore/truststore files as well as their type (JKS or PKCS12):

    REDIS_HOST: <replaceme>
    ACTIVEMQ_BROKER_URL: tcp://<replaceme>:<replaceme>
    ACTIVEMQ_DESTINATION_QUEUE_NAME: stage.statistik.utlatande.queue
    NTJP_WS_CERTIFICATE_FILE=${CERTIFICATE_FOLDER}/demo.intygstjanst.intygstjanster.se.p12
    NTJP_WS_TRUSTSTORE_FILE=${CERTIFICATE_FOLDER}/truststore.jks
    NTJP_WS_CERTIFICATE_TYPE: PKCS12
    NTJP_WS_TRUSTSTORE_TYPE: JKS
    
The _stage/config/certificate.properties_ file shouldn't require any changes **except** modifying _localhost.stub.url=_ depending on whether you want communication with FK to be stubbed or not:

    localhost.stub.url=URL to FK stub OR NTjP QA
    
The _stage/config/recipients.json_ file may need to be updated with any new intyg recipients.
    
    
### 2.3 Prepare certificates
Staging and Prod certificates are **never** commited to git. However, you may temporarily copy them to _stage/certifikat_ in order to install/update them. Typically, certificates have probably been installed separately. The important thing is that the deployment template **requires** a secret named:

    intygstjanst-stage-certifikat
    
to be available in the OpenShift project. It will be mounted to _/opt/intygstjanst-stage/certifikat_ in the container file system.

### 2.4 Creating config and secrets
If you've finished updating the files above, it's now time to use _oc_ to install them into OpenShift.

All commands must be executed from the same folder as this markdown file, i.e. _/intygstjanst/devops/openshift_    

##### 2.4.1 Create environment variables secret and configmap
From YAML-files, their names are hard-coded into the respective file

    oc create -f stage/configmap-vars.yaml
    oc create -f stage/secret-vars.yaml
    
##### 2.4.2 Create env secret and config map
Creates config map and secret from the contents of the _stage/env_ and _stage/config_ folders:

    oc create configmap "intygstjanst-stage-config" --from-file=stage/config/
    oc create secret generic "intygstjanst-stage-env" --from-file=stage/env/ --type=Opaque
    
##### 2.4.3 Create secret with certificates
If this hasn't been done previously, you may **temporarily** copy keystores into the _stage/certifikat_ folder and then install them into OpenShift using this command:

    oc create secret generic "intygstjanst-stage-certifikat" --from-file=stage/certifikat/ --type=Opaque

### 2.5 Deploy
We're all set for deploying the application. As stated in the pre-reqs, the "deploytemplate-webapp" must be installed in the OpenShift project.

**NOTE 1!!** You need to reference the correct docker image from the Nexus!! You must replace \<replaceme\> with a correct path to the image to deploy!!

**NOTE 2!!** Please specify the DATABASE_NAME for the intygstjanst stage env mysql.
Run the following command:

    oc process deploytemplate-webapp \
        -p APP_NAME=intygstjanst-stage \
        -p IMAGE=<replaceme> \
        -p STAGE=stage -p DATABASE_NAME=<replaceme> \
        -p HEALTH_URI=/inera-certificate/services \
        -o yaml | oc apply -f -

### 2.6 Verify
The pod(s) running intygstjanst should become available within a few minutes.

### 2.7 Routes
Intygstjänsten should _only_ be accessible from inside the OpenShift project using its _service_ name (e.g. http://intygstjanst-stage:8080) and from Nationella tjänsteplattformen. E.g. take care when setting up an OpenShift route so Intygstjänsten isn't publically adressable from the Internet.

The security measures based on mutual TLS and PKI should nevertheless stop any attempts from unsolicited callers.
