import groovy.json.JsonSlurper

def portainerDeployment() {
    def accessToken = getAccessToken(
                        'https://portainer.kvhome.in/api/auth',
                        PORT_CREDS_USR,
                        PORT_CREDS_PSW
                        ).jwt;

    createStackUsingRepository(
        accessToken,
        'dota2-winrate-service',
        'https://github.com/Kaivalya461/kubernetes-yamls',
        'refs/heads/master',
        'Dota2-Winrate-App/deploy.yaml'
    );
}

@Library("my-shared-library") _

pipeline {
    environment {
        imagename = "kaivalya461/dota2-winrate-app:latest"
        dockerImage = ''
        PORT_CREDS = credentials('portainer-app-user')
    }

    agent any

    tools {
        // Install the Maven version configured as "my-maven" and add it to the path.
        maven 'my-maven'
    }

    stages {
        stage('Build') {
            steps {
                echo 'Building..'

                // Get some code from a GitHub repository
                // Run Maven on a Unix agent.
                git 'https://github.com/Kaivalya461/dota2-model.git'
                sh "mvn -Dmaven.test.failure.ignore=true clean install"

                git 'https://github.com/Kaivalya461/dota2-steam-service-commons.git'
                sh "mvn -Dmaven.test.failure.ignore=true clean install"

                git 'https://github.com/Kaivalya461/dota2-winrate-service.git'
                sh "mvn -Dmaven.test.failure.ignore=true clean install"

                echo 'Building stage finished.'
            }
        }

        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }

        stage('DockerBuildImage') {
            steps {
                echo 'Build Docker Image..'
                script{
                    dockerImage = docker.build(imagename)
                }
            }
        }

        stage('DockerPush') {
            steps {
                echo 'Pushing to DockerHub....'
                //Add DockerHub Creds in Jenkins Cred Manager, and use the generated credId below
                script{
                    withDockerRegistry([ credentialsId: "kv-dockerhub-user", url: '' ]) {
                        dockerImage.push()
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }

        stage ('Docker CleanUp') {
            steps {
                echo 'CleanUp..'
                sh 'docker images'
                sh 'docker rmi ' + imagename
                sh 'docker images'
            }
        }

        stage ('Portainer Deployment') {
            steps {
                echo 'Portainer Create Stack..'

                script {
                    portainerDeployment();
                }
            }
        }
    }
}