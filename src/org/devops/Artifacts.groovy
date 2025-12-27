package org.devops

// 分离的函数定义
def GetMavenProjectInfo(pomPath = 'pom.xml',module='') {
    def info = [:]
    
    info.artifactId = sh(
        script: "mvn help:evaluate -Dexpression=project.artifactId -f ${pomPath} -q -DforceStdout",
        returnStdout: true
    ).trim()
    
    info.version = sh(
        script: "mvn help:evaluate -Dexpression=project.version -f ${pomPath} -q -DforceStdout",
        returnStdout: true
    ).trim()
    
    info.groupId = sh(
        script: "mvn help:evaluate -Dexpression=project.groupId -f ${pomPath} -q -DforceStdout",
        returnStdout: true
    ).trim()
    
    info.finalName = sh(
        script: "mvn help:evaluate -Dexpression=project.build.finalName -f ${pomPath} -q -DforceStdout",
        returnStdout: true
    ).trim()

    // 获取打包类型
    info.packaging = sh(
        script: "mvn help:evaluate -Dexpression=project.packaging -f ${pomPath} -q -DforceStdout",
        returnStdout: true
    ).trim()
    
    
    // 动态构建文件名
    info.fileName = "${info.finalName}.${info.packaging}"
    if (module) {
        info.filePath = "${module}/target/${info.fileName}"
    } else {
        info.filePath = "target/${info.fileName}"
    }
    info.fullName = "${info.groupId}:${info.artifactId}:${info.version}"
    
    return info
}

def DeployMavenArtifact(module='', repoUrl='', repoId='mymaven', pomPath='pom.xml') {
    
    // 获取项目信息
    if (module) {
        pomPath="${module}/${pomPath}"
    }
    def projectInfo = GetMavenProjectInfo(pomPath, module)

    def targetRepoUrl = repoUrl
    if (!targetRepoUrl) {
        if (projectInfo.version.toUpperCase().contains('SNAPSHOT')) {
            // SNAPSHOT 版本 → snapshots 仓库
            targetRepoUrl = 'http://dxnexus.ciicsh.com/repository/maven-snapshots/'
        } else {
            // Release 版本 → releases 仓库
            targetRepoUrl = 'http://dxnexus.ciicsh.com/repository/maven-releases/'
        }
    }
    
    echo "📦 部署信息:"
    echo "  GroupId: ${projectInfo.groupId}"
    echo "  ArtifactId: ${projectInfo.artifactId}"
    echo "  Version: ${projectInfo.version}"
    echo "  Packaging: ${projectInfo.packaging}"
    echo "  文件: ${projectInfo.filePath}"
    echo "🚀 开始上传 Maven 制品到 Nexus..."

    // 检查文件是否存在
    if (!fileExists(projectInfo.filePath)) {
        error("❌ 文件不存在: ${projectInfo.filePath}，请先执行 Maven 构建！")
    }

    // 执行部署
    sh """
        mvn deploy:deploy-file \\
            -DgeneratePom=false \\
            -DrepositoryId=${repoId} \\
            -Dfile=${projectInfo.filePath} \\
            -Durl=${targetRepoUrl} \\
            -DpomFile=${pomPath} \\
            -Dpackaging=${projectInfo.packaging}
    """
    
    echo "✅ 制品 ${projectInfo.fullName} 部署成功!"
}

//上传制品
def PushRawArtifacts(project, appName, appType, module='', repoName='mylocalrepo'){
    def targetDir="/${project}/${appName}/${env.ARTIFACT_VERSION}"
    def version="${env.ARTIFACT_VERSION}"
    switch(appType){
        case "maven":
            filePath = module ? "${module}/target" : "target"
            pkgName="${appName}-${version}.jar"
            opkgName = sh returnStdout: true, script: "ls ${filePath}/*.jar | head -1 | xargs basename"
            opkgName = opkgName.trim()  // 关键！去掉换行符
            sh """
                cd ${filePath}
                cp ${opkgName} ${pkgName}
            """
            break;
        case "npm":
            filePath="dist"
            pkgName="${appName}-${version}.tar.gz"
            sh """
                cd ${filePath}
                tar zcf ${pkgName} *
            """
        default:
            error: "No such tools ... [maven/]"
            break;
    }  
    sh """
        ls -l 
        curl -X POST "http://dxnexus.ciicsh.com/service/rest/v1/components?repository=${repoName}" \\
        -H 'accept: application/json' \\
        -H 'Content-Type: multipart/form-data' \\
        -F "raw.directory=${targetDir}" \\
        -F "raw.asset1=@${filePath}/${pkgName}" \\
        -F "raw.asset1.filename=${pkgName}" \\
        -u "admin":"S_OjBYy14J"
    """
}

//下载制品
def PullRawArtifacts(version, project, appName, appType, repoName='mylocalrepo'){
    repoUrl = "http://dxnexus.ciicsh.com/repository/${repoName}/"

    if ("${appType}" == "maven"){
        type="jar"
    }
    if ("${appType}" == "npm"){
        type="tar.gz"
    }

    pkgPath = "${repoUrl}/${project}/${appName}/${version}/${appName}-${version}.${type}"
    sh """
        wget --http-user=admin --http-passwd=S_OjBYy14J ${pkgPath}
    """
}

//上传镜像
def PushDockerArtifacts(image_project, image_repo, image_tag, harbor_url){
    def harbor_url = harbor_url ? "${harbor_url}" : "prd-ops-harbor03.ciicsh.com"

    sh """
        #登录镜像仓库
        docker login ${harbor_url} -u admin -p 7F#SanTGqG6E

        #构建镜像
        docker build -t ${harbor_url}/${image_project}/${image_repo}:${image_tag} .

        #上传镜像
        docker push ${harbor_url}/${image_project}/${image_repo}:${image_tag}

        #删除镜像
        sleep 2
        docker rmi ${harbor_url}/${image_project}/${image_repo}:${image_tag}
    """
}


