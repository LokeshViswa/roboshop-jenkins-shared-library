def call() {
    try {
        pipeline {

            agent {
                label 'workstation'
            }

            stages {

                stage('Compile/Build') {
                    steps {
                        script {
                            common.compile()
                        }
                    }
                }

                stage('Unit Tests') {
                    steps {
                        script {
                            common.unittests()
                        }
                    }
                }

                stage('Quality Control') {
                    steps {
                        withAWSParameterStore(credentialsId: '', naming: 'relative', path: '/service', recursive: true, regionName: 'eu-west-1') {
                            echo "USER = ${env.SONARQUBE_USER}"
                        }

                    }
                }

                stage('Upload Code to Centralized Place') {
                    steps {
                        echo 'Upload'
                    }
                }


            }

        }
    } catch (Exception e) {
      common.email("Failed")
    }
}

