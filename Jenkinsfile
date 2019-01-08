node {
   def mvnHome
   def mavenProfiles = ""
   def ideTests = false
   if (env.JOB_NAME.endsWith("release-ide")) {
     mavenProfiles = "-Prelease-ide-composite,deploy-ide-composite"
   } else if (env.JOB_NAME.endsWith("release")) {
     mavenProfiles = "-Prelease-composite"
   } else if (env.JOB_NAME.endsWith("ide-tests")) {
     mavenProfiles = "-Pbuild-ide,test-ide"
     ideTests = true
   }
   properties([
     [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '30']]
   ])
   stage('Checkout') { // for display purposes
      checkout scm
   }
   stage('Build') {
      wrap([$class: 'Xvfb', autoDisplayName: true, debug: false]) {
        if (ideTests) {
          sh "mutter --replace --sm-disable 2> mutter.err &"
        }
        // Run the maven build
        // don't make the build fail in case of test failures...
        sh "./mvnw -Dmaven.test.failure.ignore=true -fae clean verify ${mavenProfiles}"
      }
   }
   stage('Results') {
      // ... JUnit archiver will set the build as UNSTABLE in case of test failures
      junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
   }
}
