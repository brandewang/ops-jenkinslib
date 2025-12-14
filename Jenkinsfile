@Library("mylib@main") _
import org.devops.checkout

def checkout = new Checkout()

pipeline {
    agent { lable "build" }
    stages {
        stage("CHeckout"){
            steps {
                script {
                    checkout.GetCode("${env.srcUrl}", "${env.branchName}")
                }
            }
        }
    }
}