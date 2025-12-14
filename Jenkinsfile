@Library("mylib@main") _
import org.devops.*

def checkout = new Checkout()
def build = new Build()

pipeline {
    agent { lable "build" }
    stages {
        stage("Checkout"){
            steps {
                script {
                    checkout.GetCode("${env.srcUrl}", "${env.branchName}")
                }
            }
        }

        stage("Build"){
            steps {
                script {
                    build.CodeBuild("maven")
                }
            }
        }
    }
}