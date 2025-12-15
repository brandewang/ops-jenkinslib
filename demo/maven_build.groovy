 try {
    //gitlab传递的数据
    println("${WebhookData}")

    //数据格式化
    webHookData = readJSON text: "${WebhookData}"

    //提取仓库信息
    env.srcUrl = webHookData["project"]["git_http_url"]     //项目地址
    env.branchName = webHookData["ref"] - "refs/heads/"    //分支
    env.commitId = webHookData["checkout_sha"]             //提交id
    env.commitUser = webHookData["user_username"]           //提交人
    env.userEmail = webHookData["user_email"]               //邮箱

    currentBuild.description = "Trigger by Gitlab \n branch: ${env.branchName} \n user: ${env.commitUser}"
    currentBuild.displayName = "${env.commitId}"
 } catch(e){
    print(e)
    currentBuild.description = "Trigger by Jenkins"
 }

pipeline {
    agent { label "build" }

    stages {
        stage("CheckOut"){
            steps{
                script {
                    //仓库信息
                    // branchName = "${params.branchName}"
                    // srcUrl = "${params.srcUrl}"

                    //下载代码
                    checkout scmGit(branches: [[name: "${env.branchName}"]], extensions: [], userRemoteConfigs: [[credentialsId: '9eef3cd8-5374-4368-8a70-d1791640dc11', url: "${env.srcUrl}"]])

                    //验证
                    sh "ls -l"
                }
            }
        }
        // 代码构建
        stage("Build"){
            steps{
                script{
                    // echo "build"
                    sh "mvn clean package -DskipTests -s settings.xml"
                }
            }
        }
        // 单元测试
        stage("UnitTest"){
            steps{
                script{
                    // echo "unit test"
                    sh "mvn test -s settings.xml"
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
    }
    post {
        always{
            script {
                // emailext body: 'hello world!......jenkins', subject: 'test......', to: 'wangysh@ciicsh.com'
                EmailUser("wangysh@ciicsh.com")
            }
        }        
    }
}

//发送邮件
def EmailUser(userEmail){
    emailext(
                subject: "构建通知: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                to: userEmail,
                body: """
                <html>
                <body>
                    <p><strong>项目:</strong> ${env.JOB_NAME}</p>
                    <p><strong>构建号:</strong> #${env.BUILD_NUMBER}</p>
                    <p><strong>状态:</strong> <span style="color: ${currentBuild.currentResult == 'SUCCESS' ? 'green' : 'red'}; font-weight: bold;">${currentBuild.currentResult}</span></p>
                    <p><strong>时间:</strong> ${new Date().format('yyyy-MM-dd HH:mm:ss')}</p>
                    <p><strong>详情:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    <hr>
                    <p style="color: gray; font-size: 12px;">Jenkins 自动通知</p>
                </body>
                </html>
                """,
                mimeType: 'text/html'
    )
}
