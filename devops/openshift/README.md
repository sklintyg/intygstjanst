# Deployment instructions

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
    > oc project intygdevtest

(3.) Make sure the latest deployment template is installed into the cluster, see https://github.com/sklintyg/tools/blob/develop/devops/openshift/deploytemplate-webapp.yaml

    
## DINTYG setup

##### Create pipeline

    ~/intyg/oc/./oc process pipelinetemplate-test-webapp -p APP_NAME=intygstjanst-test -p STAGE=test -p SECRET=nosecret -p TESTS="restAssuredTest" -p CONTEXT_PATH="inera-certificate" -p HEALTH_URI="/inera-certificate/services" | ~/intyg/oc/./oc apply -f -
    
##### Create env var secret and config map

    oc create -f test/configmap-vars.yaml
    oc create -f test/secret-vars.yaml
    
##### Create file secret and config map

    oc create configmap "intygstjanst-test-config" --from-file=test/config/
    oc create secret generic "intygstjanst-test-env" --from-file=test/env/ --type=Opaque
    oc create secret generic "intygstjanst-test-certifikat" --from-file=test/certifikat/ --type=Opaque
    
##### Run pipeline
Typically triggered from Jenkins, but it's possible to trigger a pipeline manually, just remember to specify the required parameters.

## devtest deploy

- If necessary, open devtest/secret-vars.yaml and replace <placeholder> with real values. Save, but do **not** commit. 
- Make sure _devtest/configmap-vars.yaml_ contains the expected env vars.

##### Create env var secret and config map

    oc create -f devtest/configmap-vars.yaml
    oc create -f devtest/secret-vars.yaml
    
##### Create file secret and config map

    oc create configmap "intygstjanst-devtest-config" --from-file=devtest/config/
    oc create secret generic "intygstjanst-devtest-env" --from-file=devtest/env/ --type=Opaque
    
##### Create certificate secret (optional)
Certificates are typically pre-installed into the devtest "project". If necessary, the JKS / P12 files can be copied into _devtest/certifikat_ and then installed into the project:    
    
    oc create secret generic "intygstjanst-devtest-certifikat" --from-file=devtest/certifikat/ --type=Opaque

##### Deploy för NMT till tintyg

    oc process -f ~/intyg/tools/devops/openshift/deploytemplate-webapp-new.yaml \
    -p APP_NAME=intygstjanst-devtest \
    -p IMAGE=docker-registry.default.svc:5000/dintyg/intygstjanst-verified:latest \
    -p STAGE=devtest -p DATABASE_NAME=intygstjanstdevtest \
    -p HEALTH_URI=/inera-certificate/services \
    -o yaml | oc apply -f -


# DEMO från scratch

Se till att du är inloggad med CLI mot project "demointyg"

### 1. Resurser
Skapa upp följande resurser via templates:

- ActiveMQ (persistent)
- MySQL (persistent)
- Redis (flyktig)

För MySQL och Redis, låt OCP skapa en secret som kan bindas till för lösenord etc.

Använd "admin" som användarnamn och lösenord för ActiveMQ och MySQL, "redis" för redis.

Om man skapar mysql från scratch kan man behöva sätta upp användare och scheman i MySQL:

    

##### 1.1 Deploy template
Vår deploytemplate-webapp behöver finnas installerad i aktuellt project. Från /tools/devops/openshift:

    oc project demointyg
    oc create -f deploytemplate-webapp.yaml

### 2. Config och Secrets
Gå till /devops/openshift i filsystemet.
       
##### 2.1 Create secrets and config maps
Hemliga resurser (keystores, lösenord till keystores etc.) kan behöva kopieras in för hand i respektive mapp och sedan plockas bort.

    oc create -f demo/configmap-vars.yaml
    oc create -f demo/secret-vars.yaml
    oc create configmap "intygstjanst-demo-config" --from-file=demo/config/
    oc create secret generic "intygstjanst-demo-env" --from-file=demo/env/ --type=Opaque
    oc create secret generic "intygstjanst-demo-certifikat" --from-file=demo/certifikat/ --type=Opaque

### 3. Sätt upp deployment
Deployment skall triggas på varje dintyg.intygstjanst-test-verified.
    
    oc process deploytemplate-webapp \
        -p APP_NAME=intygstjanst-demo \
        -p IMAGE=docker-registry.default.svc:5000/dintyg/intygstjanst-test-verified:latest \
        -p STAGE=demo -p DATABASE_NAME=intyg \
        -p HEALTH_URI=/inera-certificate/services \
        -o yaml | oc apply -f -

Man vill eventuellt lägga till en trigger. Det kan ske direkt i "Edit YAML"

     triggers:
        - imageChangeParams:
            automatic: true
            containerNames:
              - intygstjanst-demo
            from:
              kind: ImageStreamTag
              name: 'intygstjanst-test-verified:latest'
              namespace: dintyg
          type: ImageChange
        - type: ConfigChange
