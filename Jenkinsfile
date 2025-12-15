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

    parameters {
        string(name: 'SRC_URL', defaultValue: 'http://gitlab.ciicsh.com/ops_group/devops03-maven-service.git', description: '源代码仓库URL')
        string(name: 'SRC_BRANCH', defaultValue: 'master', description: '代码分支')
        string(name: 'CONFIG_URL', defaultValue: 'http://gitlab.ciicsh.com/ops_group/devops3-jenkinslib-service.git', description: '配置仓库URL')
        string(name: 'CONFIG_BRANCH', defaultValue: 'main', description: '配置分支')
    }

    environment {
        // 将参数转为环境变量
        SRC_URL = "${params.SRC_URL}"
        BRANCH_NAME = "${params.SRC_BRANCH}"
        CONF_URL = "${params.CONFIG_URL}"
        CONF_BRANCH = "${params.CONFIG_BRANCH}"
    }

    stages {
        stage("Checkout"){
            steps {
                cleanWs()
                dir('config'){
                    script {
                        checkout.GetCode("${env.CONF_URL}", "${env.CONF_BRANCH}")
                        sh 'pwd && ls -l'
                    }
                }
                dir('code'){
                    script {
                        checkout.GetCode("${env.SRC_URL}", "${env.BRANCH_NAME}")
                        sh 'pwd && ls -l'
                    }
                }

            }
        }

        stage("Build"){
            steps {
                dir('code'){
                    script {
                        sh 'pwd && ls -l'
                        build.CodeBuild("maven")
                    }
                }
            }
        }

        stage("UnitTest"){
            steps {
                dir('code'){
                    script {
                        unittest.CodeTest("maven")
                    }
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