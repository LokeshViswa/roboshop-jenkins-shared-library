def call() {

    pipeline {

        agent any

        parameters {
            string(name: 'APP_ENV', defaultValue: '', description: 'Enter Env like dev or prod')
            string(name: 'COMPONENT', defaultValue: '', description: 'Enter component name')
            string(name: 'APP_VERSION', defaultValue: '', description: 'Enter application version to deploy')
        }

        options {
            ansiColor('xterm')
        }

        environment {
            SSH=credentials('SSH')
        }

        stages {

            stage('Run Deployment') {
                steps {
                    sh '''
            aws ssm put-parameter --name "${APP_ENV}.${COMPONENT}.APP_VERSION" --type "String" --value "${APP_VERSION}" --overwrite
            
            # this is for immutable approach 
            aws autoscaling start-instance-refresh --auto-scaling-group-name ${APP_ENV}-${COMPONENT}-asg  --preferences '{"InstanceWarmup": 240, "MinHealthyPercentage": 90, "SkipMatching": false}' 
            
            ## These are for mutable approach 
            #aws ec2 describe-instances     --filters "Name=tag:Name,Values=${APP_ENV}-${COMPONENT}"  | jq ".Reservations[].Instances[].PrivateIpAddress" >/tmp/hosts
            #ansible-playbook -i /tmp/hosts deploy.yml -e component=${COMPONENT} -e env=${APP_ENV} -e ansible_user=${SSH_USR} -e ansible_password=${SSH_PSW}
          '''
                }
            }

        }

    }

}