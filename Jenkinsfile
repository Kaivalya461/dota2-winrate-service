pipeline {
    environment {
        imagename = "kaivalya461/dota2-winrate-app:latest"
        dockerImage = ''
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
        stage ('CleanUp') {
            steps {
                echo 'CleanUp..'
                sh 'docker images'
                sh 'docker rmi ' + imagename
                sh 'docker images'
            }
        }
    }
}