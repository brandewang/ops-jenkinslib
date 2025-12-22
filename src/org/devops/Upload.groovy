package org.devops

// 分离的函数定义
def getMavenProjectInfo(pomPath = 'pom.xml',module='') {
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

def deployMavenArtifact(repoUrl='', repoId='mymaven', pomPath='pom.xml', module='') {
    
    // 获取项目信息
    if (module) {
        pomPath="${module}/${pomPath}"
    }
    def projectInfo = getMavenProjectInfo(pomPath, module)

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