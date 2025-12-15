@Library("mylib@main") _
import org.devops.*

def checkout = new Checkout()
def build = new Build()
def unittest = new UnitTest()
def notified = new Notified()
def confUrl = 'http://gitlab.ciicsh.com/ops_group/devops3-jenkinslib-service.git'
def confBranch = 'main'

pipeline {
    agent { label "build" }
    options {
        skipDefaultCheckout true
    }
    stages {
        stage("Checkout"){
            steps {
                cleanWs()
                dir('config'){
                    script {
                        checkout.GetCode("${env.conUrl}", "${env.confBranch}")
                        sh 'pwd && ls -l'
                    }
                }
                dir('code'){
                    script {
                        checkout.GetCode("${env.srcUrl}", "${env.branchName}")
                        sh 'pwd && ls -l'
                    }
                }
            }
        }

        stage("Build"){
            steps {
                script {
                    sh 'pwd && ls -l'
                    build.CodeBuild("maven")
                }
            }
        }

        stage("UnitTest"){
            steps {
                script {
                    unittest.CodeTest("maven")
                }
            }
        }
    }
    post {
        always{
            script {
                notified.SendEmail("wangysh@ciicsh.com")
            }
        }        
    }
}