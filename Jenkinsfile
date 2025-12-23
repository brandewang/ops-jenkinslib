@Library("mylib@main") _
import org.devops.*

// ========== 工具类初始化 ==========
def checkout = new Checkout()
def build = new Build()
def unittest = new UnitTest()
def notified = new Notified()
def artifacts = new Artifacts()


// ========== 配置变量 ==========
def DEFAULT_SRC_URL = 'http://gitlab.ciicsh.com/ops_group/devops03-maven-service.git'
def DEFAULT_SRC_BRANCH = 'master'
def DEFAULT_CONFIG_URL = 'http://gitlab.ciicsh.com/ops_group/devops3-jenkinslib-service.git'
def DEFAULT_CONFIG_BRANCH = 'main'
def DEFAULT_HARBOR_URL = 'prd-ops-harbor03.ciicsh.com'
def DEFAULT_USER_EMAIL = 'wangysh@ciicsh.com'

// ========== 应用变量 ==========
def app = ['build_type': 'maven', 'module': '', 'artifact_upload': true, 'docker_build': true, 'image_project': 'devops', 'image_repo': 'devops03-maven-servie']

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

    triggers {
        GenericTrigger(
            causeString: 'Generic Cause',
            genericVariables: [
                [
                    defaultValue: '',
                    key: 'WebhookData',
                    regexpFilter: '',
                    value: '$'
                ]
            ],
            regexpFilterExpression: '',
            regexpFilterText: '',
            token: 'devops03-maven-service-lib',
            tokenCredentialId: ''
        )
    }
    options {
        skipDefaultCheckout true
        timestamps()
    }

    parameters {
        // string(name: 'PARAMS_SRC_URL', defaultValue: DEFAULT_SRC_URL, description: '源代码仓库URL')
        choice(
        name: 'PARAMS_SRC_URL',
        choices: [
            DEFAULT_SRC_URL
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
                    sh "cp -r ${env.WORKSPACE}/config/${env.JOB_NAME}/* ${env.WORKSPACE}/code/"
                }
            }
        }

        stage("Build"){
            steps {
                dir('code'){
                    script {
                        build.CodeBuild("${app.build_type}","${app.module}")
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

        stage('UploadArtifact'){
            when {
                expression { 
                    app.artifact_upload == true 
                }
            }
            steps {
                dir('code') {
                    script {                                       
                        // 上传到 Maven 仓库
                        artifacts.deployMavenArtifact("${app.module}")                 
                    }

                }
            }
        }

        stage('DockerBuild'){
            when {
                expression {
                    app.docker_build == true
                }
            }
            steps {
                dir("code/${app.module}") {
                    script {

                        sh """
                            #登录镜像仓库
                            docker login ${DEFAULT_HARBOR_URL} -u admin -p 7F#SanTGqG6E

                            #构建镜像
                            docker build -t ${DEFAULT_HARBOR_URL}/${app.image_project}/${app.image_repo}:${env.IMAGE_TAG} .

                            #上传镜像
                            docker push ${DEFAULT_HARBOR_URL}/${app.image_project}/${app.image_repo}:${env.IMAGE_TAG}

                            #删除镜像
                            sleep 2
                            docker rmi ${DEFAULT_HARBOR_URL}/${app.image_project}/${app.image_repo}:${env.IMAGE_TAG}
                        """
                    }
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
                }
            }
        }        
    }
}