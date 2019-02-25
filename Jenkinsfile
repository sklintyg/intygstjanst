#!groovy

def buildVersion = "3.9.0.${BUILD_NUMBER}"
def buildRoot = JOB_BASE_NAME.replaceAll(/-.*/, "") // Keep everything up to the first dash

def commonVersion = "3.10.0.+"
def infraVersion = "3.10.0.+"
def refDataVersion = "1.0-SNAPSHOT"
def versionFlags = "-DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DinfraVersion=${infraVersion} -DrefDataVersion=${refDataVersion}"

stage('checkout') {
    node {
        git url: "https://github.com/sklintyg/intygstjanst.git", branch: GIT_BRANCH
        util.run { checkout scm }
    }
}

stage('build') {
    node {
        try {
            shgradle "--refresh-dependencies clean build testReport sonarqube -PcodeQuality -DgruntColors=false \
                  ${versionFlags}"
        } finally {
            publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/allTests', \
                reportFiles: 'index.html', reportName: 'JUnit results'
        }
    }
}

stage('tag') {
    node {
        shgradle "tagRelease ${versionFlags}"
    }
}

stage('propagate') {
    node {
        gitRef = "v${buildVersion}"
        releaseFlag = "${GIT_BRANCH.startsWith("release")}"
        build job: "intygstjanst-dintyg-build", wait: false, parameters: [
                [$class: 'StringParameterValue', name: 'INTYGSTJANST_BUILD_VERSION', value: buildVersion],
                [$class: 'StringParameterValue', name: 'COMMON_VERSION', value: commonVersion],
                [$class: 'StringParameterValue', name: 'INFRA_VERSION', value: infraVersion],
                [$class: 'StringParameterValue', name: 'REF_DATA_VERSION', value: refDataVersion],
                [$class: 'StringParameterValue', name: 'GIT_REF', value: gitRef],
                [$class: 'StringParameterValue', name: 'RELEASE_FLAG', value: releaseFlag]
        ]
        build job: "${buildRoot}-deploy-it-webcert", wait: false, parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH', value: GIT_BRANCH]]
        build job: "${buildRoot}-deploy-it-minaintyg", wait: false, parameters: [[$class: 'StringParameterValue', name: 'GIT_BRANCH', value: GIT_BRANCH]]
    }
}

stage('notify') {
    node {
        util.notifySuccess()
    }
}
