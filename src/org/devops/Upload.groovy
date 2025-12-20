package org.devops

//Maven
def getMavenProjectInfo(pomPath = 'pom.xml') {
    def info = [:]
    
    // 方法1：使用 mvn 命令（推荐，不需要额外插件）
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
    
    // 构建文件名
    info.jarFile = "target/${info.artifactId}-${info.version}.jar"
    info.fullName = "${info.groupId}:${info.artifactId}:${info.version}"
    
    return info
}

def deployMavenArtifact(repoUrl, repoId, filePath, pomPath = 'pom.xml') {
        echo "🚀 开始上传 Maven 制品到 Nexus..."

        // 执行部署
        sh """
            mvn deploy:deploy-file \\
            -DgeneratePom=false \\
            -DrepositoryId=${repoId}  \\
            -Dfile=${filePath} \\
            -Durl=${repoUrl} \\
            -DpomFile=${pomPath} 
        """
        
        echo "✅ 制品部署成功: ${projectInfo.fullName}"
        
}