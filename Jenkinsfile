#!groovy

def buildVersion  = "3.0.${BUILD_NUMBER}"
def commonVersion = "3.0.+"
def typerVersion  = "3.0.+"

stage('checkout') {
    node {
        util.run { checkout scm }
    }
}

stage('build') {
    node {
        shgradle "--refresh-dependencies clean build sonarqube -PcodeQuality -DgruntColors=false \
                  -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DtyperVersion=${typerVersion}"
    }
}

stage('deploy') {
    node {
        util.run {
            ansiblePlaybook extraVars: [version: buildVersion, ansible_ssh_port: "22", deploy_from_repo: "false"], \
                installation: 'ansible-yum', \
                inventory: 'ansible/hosts_test', \
                playbook: 'ansible/deploy.yml', \
                sudoUser: null
        }
    }
}

stage('integration tests') {
    node {
        shgradle "restAssuredTest -DbaseUrl=http://intygstjanst.inera.nordicmedtest.se/ \
                 -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DtyperVersion=${typerVersion}"
    }
}

stage('tag and upload') {
    node {
        shgradle "uploadArchives tagRelease -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DtyperVersion=${typerVersion}"
    }
}
