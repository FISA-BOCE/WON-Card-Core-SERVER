pipeline {
    agent any

    environment {
        WAS_HOST = "${env.CARD_CORE_WAS_HOST}"
        WAS_USER = "${env.CARD_CORE_WAS_USER}"
        APP_DIR = "${env.CARD_CORE_APP_DIR}"
        SSH_CREDENTIAL_ID = "${env.CARD_CORE_SSH_CREDENTIAL_ID}"
        JAR_NAME = "won-card-core-${BUILD_NUMBER}.jar"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean bootJar -x test'
                sh 'cp build/libs/*.jar ${JAR_NAME}'
            }
        }

        stage('Transfer') {
            steps {
                sshagent(credentials: [env.SSH_CREDENTIAL_ID]) {
                    sh '''
                    scp -o StrictHostKeyChecking=no "${JAR_NAME}" "${WAS_USER}@${WAS_HOST}:/tmp/"
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sshagent(credentials: [env.SSH_CREDENTIAL_ID]) {
                    sh '''
                    ssh -o StrictHostKeyChecking=no "${WAS_USER}@${WAS_HOST}" "
                      sudo mv /tmp/${JAR_NAME} ${APP_DIR}/releases/ &&
                      sudo chown deploy:deploy ${APP_DIR}/releases/${JAR_NAME} &&
                      cd ${APP_DIR} &&
                      sudo -u deploy ln -sfn releases/${JAR_NAME} app.jar &&
                      sudo systemctl restart won-card-core &&
                      for i in 1 2 3 4 5 6; do
                        curl -f http://localhost:8083/actuator/health && exit 0
                        sleep 5
                      done
                      sudo systemctl status won-card-core --no-pager
                      exit 1
                    "
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'CI/CD deployment succeeded.'
        }
        failure {
            echo 'CI/CD deployment failed.'
        }
    }
}
