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
                    echo "build"
                }
            }
        }
        // 单元测试
        stage("UnitTest"){
            steps{
                script{
                    echo "build"
                }
            }
        }
    }
}

