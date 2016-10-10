#!groovy

def javaEnv() {
    def javaHome = tool 'JDK8u66'
    ["PATH=${env.PATH}:${javaHome}/bin", "JAVA_HOME=${javaHome}"]
}

stage('checkout') {
    node {
        checkout scm
    }
}

stage('build') {
    node {
        withEnv(javaEnv()) {
            sh './gradlew --refresh-dependencies clean build sonarqube -PcodeQuality'
        }
    }
}

stage('deploy') {
    node {
        ansiblePlaybook extraVars: [version: "3.0.$BUILD_NUMBER", ansible_ssh_port: "22", deploy_from_repo: "false"], \
            installation: 'ansible-yum', \
            inventory: 'ansible/hosts_test', \
            playbook: 'ansible/deploy.yml', \
            sudoUser: null
    }
}

stage('integrationtests') {
    node {
        withEnv(javaEnv()) {
            sh './gradlew restAssuredTest -DbaseUrl=http://intygstjanst.inera.nordicmedtest.se/'
        }
    }
}

stage('tag and upload') {
    node {
        withEnv(javaEnv()) {
            sh './gradlew uploadArchives tagRelease -DnexusUsername=$NEXUS_USERNAME -DnexusPassword=$NEXUS_PASSWORD \
                -DgithubUser=$GITHUB_USERNAME -DgithubPassword=$GITHUB_PASSWORD'
        }
    }
}
