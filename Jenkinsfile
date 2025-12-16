@Library("mylib@main") _
import org.devops.*

def checkout = new Checkout()
def build = new Build()
def unittest = new UnitTest()
def notified = new Notified()


try {
    //gitlab传递的数据
    println("${WebhookData}")

    //数据格式化
    webHookData = readJSON text: "${WebhookData}"

    //提取仓库信息
    env.webhook_srcUrl = webHookData["project"]["git_http_url"]     //项目地址
    env.webhook_branchName = webHookData["ref"] - "refs/heads/"    //分支
    env.webhook_commitId = webHookData["checkout_sha"]             //提交id
    env.webhook_commitUser = webHookData["user_username"]           //提交人
    env.webhook_userEmail = webHookData["user_email"]               //邮箱

    currentBuild.description = "Trigger by Gitlab \n branch: ${env.webhook_branchName} \n user: ${env.webhook_commitUser}"
 } catch(e){
    print(e)
 }


pipeline {
    agent { label "build" }
    options {
        skipDefaultCheckout true
    }

    parameters {
        string(name: 'PARAMS_SRC_URL', defaultValue: 'http://gitlab.ciicsh.com/ops_group/devops03-maven-service.git', description: '源代码仓库URL')
        string(name: 'PARAMS_SRC_BRANCH', defaultValue: 'master', description: '代码分支')
        string(name: 'PARAMS_CONFIG_URL', defaultValue: 'http://gitlab.ciicsh.com/ops_group/devops3-jenkinslib-service.git', description: '配置仓库URL')
        string(name: 'PARAMS_CONFIG_BRANCH', defaultValue: 'main', description: '配置分支')
        string(name: 'PARAMS_USER_EMAIL', defaultValue: 'wangysh@ciicsh.com', description: '用户邮箱')
    }

    environment {
        // 将参数转为环境变量
        SRC_URL = "${env.webhook_srcUrl ?: params.PARAMS_SRC_URL}"
        SRC_BRANCH = "${env.webhook_srcBranch ?: params.PARAMS_SRC_BRANCH}"
        CONF_URL = "${params.PARAMS_CONFIG_URL}"
        CONF_BRANCH = "${params.PARAMS_CONFIG_BRANCH}"
    }

    stages {
        stage("Init"){
            steps {
                wrap([$class: 'BuildUser']) {
                    script {
                        if (env.webhook_commitUser) {
                            // Webhook 触发
                            currentBuild.description = """
                                Trigger by GitLab Webhook
                                Branch: ${env.webhook_branchName}
                                Committer: ${env.webhook_commitUser}
                                Commit: ${env.webhook_commitId?.take(8)}
                            """.stripIndent().trim()
                            currentBuild.displayName = "${env.webhook_commitId}"
                        } else {
                            // 手动触发
                            currentBuild.description = """
                                Trigger by Jenkins
                                Branch: ${env.SRC_BRANCH}
                                User: ${env.BUILD_USER}
                            """.stripIndent().trim()
                        }
                        
                        env.USER_EMAIL = "${env.webhook_userEmail ?: env.BUILD_USER_EMAIL ?: params.PARAMS_USER_EMAIL}"

                    }
                }
            }
        }
        stage("Checkout"){
            steps {
                cleanWs()
                dir('config'){
                    script {
                        checkout.GetCode("${env.CONF_URL}", "${env.CONF_BRANCH}")
                    }
                }
                dir('code'){
                    script {
                        checkout.GetCode("${env.SRC_URL}", "${env.SRC_BRANCH}")
                    }
                }

            }
        }

        stage("PrepareConfig"){
            steps {
                script {
                    sh 'pwd && ls -l'
                    echo ${env.JOB_NAME}
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
                notified.SendEmail("${env.USER_EMAIL}")
            }
        }        
    }
}