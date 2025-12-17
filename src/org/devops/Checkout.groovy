package org.devops

def GetCode(srcUrl, branchName){
    checkout scmGit(branches: [[name: branchName]], 
                    extensions: [], 
                    userRemoteConfigs: [[credentialsId: '9eef3cd8-5374-4368-8a70-d1791640dc11', 
                    url: srcUrl]])

    def commitId = sh(
        script: 'git rev-parse HEAD',
        returnStdout: true
    ).trim()

    def commitMessage = sh(
        script: 'git log -1 --pretty=format:%s',
        returnStdout: true
    ).trim()

    def title = extractCommitTitle(commitMessage)

    return [
        success: true,
        commitId: commitId,
        shortCommitId: commitId.substring(0, 8),
        title: title,
        message: commitMessage 
    ]
}
