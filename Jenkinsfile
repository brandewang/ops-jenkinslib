@Library("mylib@main") _
import org.devops.*

// ========== 工具类初始化 ==========
def checkout = new Checkout()
def build = new Build()
def unittest = new UnitTest()
def notified = new Notified()
def upload = new Upload()

// ========== 配置变量 ==========
def DEFAULT_SRC_URL = 'http://gitlab.ciicsh.com/ops_group/devops03-maven-service.git'
def DEFAULT_SRC_BRANCH = 'master'
def DEFAULT_CONFIG_URL = 'http://gitlab.ciicsh.com/ops_group/devops3-jenkinslib-service.git'
def DEFAULT_CONFIG_BRANCH = 'main'
def DEFAULT_USER_EMAIL = 'wangysh@ciicsh.com'

// ========== 应用变量 ==========
def app = ['build_type': 'maven', 
            'artifact_upload': true, 'artifact_upload_url': 'http://dxnexus.ciicsh.com/repository/maven-releases/', 'artifact_upload_repoid': 'mymaven', 
            'image_upload': false]

try {
    //gitlab传递的数据
    println("${WebhookData}")

    //数据格式化
    webHookData = readJSON text: "${WebhookData}"

    //提取仓库信息
    env.webhook_srcUrl = webHookData["project"]["git_http_url"]     //项目地址
    env.webhook_branchName = webHookData["ref"] - "refs/heads/"    //分支
    env.webhook_commitId = webHookData["checkout_sha"]             //提交id
    env.webhook_commitTitle = webHookData["title"]             //提交描述
    env.webhook_commitUser = webHookData["user_username"]           //提交人
    env.webhook_userEmail = webHookData["user_email"]               //邮箱

 } catch(e){
    print(e)
 }


pipeline {
    agent { label "build" }
    options {
        skipDefaultCheckout true
    }

    parameters {
        // string(name: 'PARAMS_SRC_URL', defaultValue: DEFAULT_SRC_URL, description: '源代码仓库URL')
        choice(
        name: 'PARAMS_SRC_URL',
        choices: [
            'http://gitlab.ciicsh.com/ops_group/devops03-maven-service.git'
        ],
        description: '源代码仓库URL'
    )
        string(name: 'PARAMS_SRC_BRANCH', defaultValue: DEFAULT_SRC_BRANCH, description: '代码分支')
        string(name: 'PARAMS_USER_EMAIL', defaultValue: DEFAULT_USER_EMAIL, description: '用户邮箱')
    }

    environment {
        // 将参数转为环境变量 并固定无法更改
        SRC_URL = "${env.webhook_srcUrl ?: params.PARAMS_SRC_URL}"
        SRC_BRANCH = "${env.webhook_branchName ?: params.PARAMS_SRC_BRANCH}"
        CONF_URL = "${DEFAULT_CONFIG_URL}"
        CONF_BRANCH = "${DEFAULT_CONFIG_BRANCH}"
    }

    stages {
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
                        def checkoutResult = checkout.GetCode("${env.SRC_URL}", "${env.SRC_BRANCH}")
                        env.SRC_COMMIT_ID = checkoutResult.shortCommitId
                        env.SRC_COMMIT_TITLE = checkoutResult.title
                        env.IMAGE_TAG = checkoutResult.tag
                    }
                }

            }
        }

        stage("PrepareConfig"){
            steps {
                script {
                    echo "${env.WORKSPACE}"
                    sh "cp ${env.WORKSPACE}/config/${env.JOB_NAME}/* ${env.WORKSPACE}/code/"
                }
            }
        }

        stage("Build"){
            steps {
                dir('code'){
                    script {
                        build.CodeBuild("${app.build_type}")
                    }
                }
            }
        }

        stage("UnitTest"){
            steps {
                dir('code'){
                    script {
                        unittest.CodeTest("${app.build_type}")
                    }
                }
            }
        }

        stage('Upload Artifact') {
            when {
                expression { 
                    // 条件1：参数控制
                    app.artifact_upload == true 
                }
            }
            steps {
                script {
                    echo "🚀 开始上传 Maven 制品到 Nexus..."
                    
                    dir('code') {
                        // 上传到 Maven 仓库
                        def mavenProjectInfo = Upload.getMavenProjectInfo(pomPath = 'pom.xml')
                        artifact_file = "${mavenProjectInfo.info.jarFile}"

                        sh """
                            mvn deploy:deploy-file \
                            -DgeneratePom=false \
                            -DrepositoryId=${app.artifact_upload_repoid}  \
                            -Dfile=target/${app.artifact_file} \
                            -Durl=${app.artifact_upload_url} \
                            -DpomFile=pom.xml 
                        """                      
                    }
                    echo "✅ 制品上传完成"
                }
            }
        }
    }
    post {
        always{
            wrap([$class: 'BuildUser']) {
                script {

                    // 设置构建描述
                    if (env.webhook_commitUser) {
                        // Webhook 触发
                        currentBuild.description = """                           
                            Title: ${env.SRC_COMMIT_TITLE}
                            Branch: ${env.webhook_branchName}
                            Committer: ${env.webhook_commitUser}
                            Commit: ${env.SRC_COMMIT_ID}
                        """.stripIndent().trim()
                        currentBuild.displayName = "#${env.BUILD_NUMBER} - Trigger by GitLab Webhook"
                    } else {
                        // 手动触发
                        currentBuild.description = """                           
                            Title: ${env.SRC_COMMIT_TITLE}
                            Branch: ${env.SRC_BRANCH}
                            User: ${env.BUILD_USER}
                            Commit: ${env.SRC_COMMIT_ID}
                        """.stripIndent().trim()
                        currentBuild.displayName = "#${env.BUILD_NUMBER} - Trigger by Jenkins"
                    }
                    
                    // 发送构建通知
                    env.USER_EMAIL = "${env.webhook_userEmail ?: env.BUILD_USER_EMAIL ?: params.PARAMS_USER_EMAIL}"
                    notified.SendEmail("${env.USER_EMAIL}")

                    //测试
                    println("${env.IMAGE_TAG}")
                    env.SRC_BRANCH = '123hellowolrd!'
                    println("${env.SRC_BRANCH}")
                }
            }
        }        
    }
}