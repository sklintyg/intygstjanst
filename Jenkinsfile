#!groovy

def javaEnv() {
  def javaHome = tool 'JDK8u66'
  ["PATH=${env.PATH}:${javaHome}/bin", "JAVA_HOME=${javaHome}"]
}

stage 'checkout'

node {
  checkout scm
}

stage 'build'

// node {
//   withEnv(javaEnv()) {
//     sh './gradlew clean install'
//   }
// }

stage 'deploy'

node {
  ansiblePlaybook extraVars: [version: "3.0.$BUILD_NUMBER"], installation: 'ansible-yum', inventory: 'ansible/hosts_test', playbook: 'ansible/deploy.yml', sudoUser: null
}

stage 'test'

node {
  withEnv(javaEnv()) {
    sh './gradlew restAssuredTest -DbaseUrl=http://intygstjanst.inera.nordicmedtest.se/'
  }
}
