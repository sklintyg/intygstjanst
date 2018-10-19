### Pre-req

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
    > oc project staging

(3.) Make sure the latest deployment template is installed into the cluster, see https://github.com/sklintyg/tools/blob/develop/devops/openshift/deploytemplate-webapp.yaml

### Update placeholders

For security reasons, no secret properties or configuration may be checked into git. Thus, a number of placeholders needs to be replaced prior to creating or updating secrets and/or config maps.

Open _staging/secret-vars.yaml and replace \<replaceme\> with expected values:

    ACTIVEMQ_BROKER_USERNAME: <replaceme>
    ACTIVEMQ_BROKER_PASSWORD: <replaceme>
    NTJP_WS_CERTIFICATE_PASSWORD: <replaceme>
    NTJP_WS_KEY_MANAGER_PASSWORD: <replaceme>
    NTJP_WS_TRUSTSTORE_PASSWORD: <replaceme>

Open _staging/configmap-vars.yaml_ and replace \<replaceme\> with expected values. You may also need to update name of keystore/truststore files as well as their type (JKS or PKCS12):

    REDIS_HOST: <replaceme>
    ACTIVEMQ_BROKER_URL: tcp://<replaceme>:<replaceme>
    ACTIVEMQ_DESTINATION_QUEUE_NAME: staging.statistik.utlatande.queue
    NTJP_WS_CERTIFICATE_FILE=${CERTIFICATE_FOLDER}/demo.intygstjanst.intygstjanster.se.p12
    NTJP_WS_TRUSTSTORE_FILE=${CERTIFICATE_FOLDER}/truststore.jks
    NTJP_WS_CERTIFICATE_TYPE: PKCS12
    NTJP_WS_TRUSTSTORE_TYPE: JKS
    
The _staging/config/certificate.properties_ file shouldn't require any changes **except** modifying _localhost.stub.url=_ depending on whether you want communication with FK to be stubbed or not:

    localhost.stub.url=URL to FK stub OR NTjP QA
    
The _staging/config/recipients.json_ file may need to be updated with any new intyg recipients.
    
    
### Certificates
Staging and Prod certificates are **never** commited to git. However, you may temporarily copy them to _staging/certifikat_ in order to install/update them. Typically, certificates have probably been installed separately. The important thing is that the deployment template **requires** a secret named:

    intygstjanst-staging-certifikat
    
to be available in the OpenShift project. It will be mounted to _/opt/intygstjanst-staging/certifikat_ in the container file system.

### Creating config and secrets
If you've finished updating the files above, it's now time to use _oc_ to install them into OpenShift.

All commands must be executed from the same folder as this markdown file, i.e. _/intygstjanst/devops/openshift_    

##### Create environment variables secret and configmap
From YAML-files, their names are hard-coded into the respective file

    oc create -f staging/configmap-vars.yaml
    oc create -f staging/secret-vars.yaml
    
##### Create env secret and config map
Creates config map and secret from the contents of the _staging/env_ and _staging/config_ folders:

    oc create configmap "intygstjanst-staging-config" --from-file=staging/config/
    oc create secret generic "intygstjanst-staging-env" --from-file=staging/env/ --type=Opaque
    
##### Create secret with certificates
If this hasn't been done previously, you may **temporarily** copy keystores into the _staging/certifikat_ folder and then install them into OpenShift using this command:

    oc create secret generic "intygstjanst-staging-certifikat" --from-file=staging/certifikat/ --type=Opaque

### Deploy
We're all set for deploying the application. As stated in the pre-reqs, the "deploytemplate-webapp" must be installed in the OpenShift project.

**NOTE 1!!** You need to reference the correct docker image from the Nexus!! You must replace \<replaceme\> with a correct path to the image to deploy!!

**NOTE 2!!** Please specify the DATABASE_NAME for the intygstjanst staging env mysql.
Run the following command:

    oc process deploytemplate-webapp \
        -p APP_NAME=intygstjanst-staging \
        -p IMAGE=<replaceme> \
        -p STAGE=staging -p DATABASE_NAME=<replaceme> \
        -p HEALTH_URI=/inera-certificate/services \
        -o yaml | oc apply -f -
