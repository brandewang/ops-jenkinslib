package org.devops

def GetCode(srcUrl, branchName){
    checkout scmGit(branches: [[name: branchName]], 
                    extensions: [], 
                    userRemoteConfigs: [[credentialsId: '9eef3cd8-5374-4368-8a70-d1791640dc11', 
                    url: srcUrl]])
}
