package org.devops

def GetCode(srcUrl, branchName){
    checkout scmGit(branches: [[name: branchName]], 
                    extensions: [], 
                    userRemoteConfigs: [[credentialsId: '24ad9e2f-a9e7-43ae-8611-bd81df2802bd', 
                    url: srcUrl]])


    // 获取完整 commit id
    def commitId = sh(
        script: 'git rev-parse HEAD',
        returnStdout: true
    ).trim()
    
    // 获取提交信息
    def commitMessage = sh(
        script: 'git log -1 --pretty=format:%s',
        returnStdout: true
    ).trim()
    
    // 提取标题（第一行）
    def title = ""
    if (commitMessage) {
        def lines = commitMessage.split('\n')
        title = lines[0].trim()
    }

    // 获取提交人
    def committer = sh(
        script: 'git log -1 --pretty="format:%an <%ae>"',
        returnStdout: true
    ).trim()

    // 提取tag
    def tag = ""
    def isTag = branchName.startsWith('refs/tags/')
    if (isTag) {
        tag = branchName - 'refs/tags/'
    } else {
        tag = "${branchName}-${commitId.substring(0, 8)}"
    }

    return [
        success: true,
        commitId: commitId,
        shortCommitId: commitId.substring(0, 8),
        title: title,
        message: commitMessage,
        committer: committer,
        tag: tag
    ]
}
