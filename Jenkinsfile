@Library("mylib@main") _
import org.devops.*

def checkout = new Checkout()
def build = new Build()

pipeline {
    agent { label "build" }
    options {
        skipDefaultCheckout true
    }
    stages {
        stage("Checkout"){
            steps {
                script {
                    checkout.GetCode("${env.srcUrl}", "${env.branchName}")
                    sh "pwd && ls -l"
                }
            }
        }

        stage("Build"){
            steps {
                script {
                    sh "pwd && ls -l"
                    build.CodeBuild("maven")
                }
            }
        }
    }
}