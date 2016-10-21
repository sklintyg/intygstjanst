#!groovy

def buildVersion = "3.1.${BUILD_NUMBER}"
def commonVersion = "3.1.+"
def typerVersion = "3.1.+"

stage('checkout') {
    node {
        util.run { checkout scm }
    }
}

stage('build') {
    node {
        try {
            shgradle "--refresh-dependencies clean build testReport sonarqube -PcodeQuality -DgruntColors=false \
                  -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DtyperVersion=${typerVersion}"
        } finally {
            publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/allTests', \
                reportFiles: 'index.html', reportName: 'JUnit results'
        }
    }
}

stage('deploy') {
    node {
        util.run {
            ansiblePlaybook extraVars: [version: buildVersion, ansible_ssh_port: "22", deploy_from_repo: "false"], \
                installation: 'ansible-yum', inventory: 'ansible/hosts_test', playbook: 'ansible/deploy.yml'
        }
	util.waitForServer('https://intygstjanst.inera.nordicmedtest.se/inera-certificate/version.jsp')
    }
}

stage('restAssured') {
    node {
        try {
            shgradle "restAssuredTest -DbaseUrl=http://intygstjanst.inera.nordicmedtest.se/ \
                  -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DtyperVersion=${typerVersion}"
        } finally {
            publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'web/build/reports/tests/restAssuredTest', \
                reportFiles: 'index.html', reportName: 'RestAssured results'
        }
    }
}

stage('tag and upload') {
    node {
        shgradle "uploadArchives tagRelease -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DtyperVersion=${typerVersion}"
    }
}
