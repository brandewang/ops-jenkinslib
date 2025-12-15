@Library("mylib@main") _
import org.devops.*

def checkout = new Checkout()
def build = new Build()
def unittest = new UnitTest()
def notified = new Notified()

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
                // emailext body: 'hello world!......jenkins', subject: 'test......', to: 'wangysh@ciicsh.com'
                notified.SendEmail("wangysh@ciicsh.com")
            }
        }        
    }
}