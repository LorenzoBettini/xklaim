node {
   def mvnHome
   def mavenProfiles = ""
   if (env.JOB_NAME.endsWith("release") {
     mavenProfiles = "-Prelease-composite"
   }
   properties([
     [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '30']]
   ])
   stage('Checkout') { // for display purposes
      checkout scm
   }
   stage('Build') {
      wrap([$class: 'Xvfb', autoDisplayName: true, debug: false]) {
        // Run the maven build
        // don't make the build fail in case of test failures...
        sh "./mvnw -Dmaven.test.failure.ignore=true -Dmaven.repo.local=.m2 -fae clean verify ${mavenProfiles}"
      }
   }
   stage('Results') {
      // ... JUnit archiver will set the build as UNSTABLE in case of test failures
      junit '**/target/surefire-reports/TEST-*.xml'
   }
}
