package org.devops


def DeployByArgocd(Map params) {
    // 检查必需参数
    if (!params.manifestsUrl) error("缺少必需参数: manifestsUrl")
    if (!params.manifestsBranch) error("缺少必需参数: manifestsBranch")
    if (!params.manifestsPath) error("缺少必需参数: manifestsPath")
    if (!params.version) error("缺少必需参数: version")

    cleanWs()
    
    def repoUrl = params.manifestsUrl.replaceFirst('^https?://', '')

    checkout scmGit(branches: [[name: params.manifestsBranch]], 
                    extensions: [], 
                    userRemoteConfigs: [[credentialsId: '24ad9e2f-a9e7-43ae-8611-bd81df2802bd', 
                    url: params.manifestsUrl]])

    // 修改镜像版本,提交到Git仓库
    withCredentials([
        usernamePassword(
            credentialsId: '24ad9e2f-a9e7-43ae-8611-bd81df2802bd',
            usernameVariable: 'GIT_USER',
            passwordVariable: 'GIT_PASS'
        )
    ]) {
        sh """
            git checkout ${params.manifestsBranch}

            # 配置 Git 用户信息（根据你的环境可能需要修改）
            git config user.email "jenkins@example.com"
            git config user.name "Jenkins"

            # 配置Git使用内存缓存
            git config --global credential.helper 'cache --timeout=300'
            
            # 第一次需要输入凭据（通过环境变量）
            (echo "username=\${GIT_USER}"; echo "password=\${GIT_PASS}") | git credential-cache store

            cd ${params.manifestsPath}
    
            # 检查文件是否存在
            if [ ! -f "${params.valueFile}" ]; then
            echo "错误: 文件 ${params.valueFile} 不存在"
            exit 1
            fi
    
            sed -i 's/tag:.*/tag: "${params.version}"/' ${params.valueFile}
            
            # 添加修改的文件
            git add ${params.valueFile}
            
            # 提交更改
            git commit -m "更新 ${params.manifestsPath} ${params.valueFile} 镜像tag为: ${params.version}"
            
            # 推送到远程仓库
            git push  http://${GIT_USER}:${GIT_PASS}@${repoUrl} ${params.manifestsBranch}
            
            echo "Git 提交和推送完成"
        """
    }
}

def RollbackByArgocd(){
    println("rollback")
}

def DeployByAnsible(Map params) {
    // 检查必需参数
    if (!params.deployType) error("缺少必需参数: deployType")
    if (!params.deployHosts) error("缺少必需参数: deployHosts")
    if (!params.project) error("缺少必需参数: deployHosts")
    if (!params.appName) error("缺少必需参数: appName")
    if (!params.releaseVersion) error("缺少必需参数: releaseVersion")
    // if (!params.targetDir) error("缺少必需参数: targetDir")


    // 2. 参数处理
    def deployHosts = params.deployHosts instanceof List ? params.deployHosts : params.deployHosts.split(',')
    def appNs = params.appNs ?: 'default'
    def appKind = params.appKind ?: 'deployment'
    
    // 3. 根据类型选择部署方法（traditional改为stand）
    switch(params.deployType) {
        case 'standard':  // 改为stand表示标准/传统部署
            deployStandard(deployHosts, params.targetDir, params.project, params.appName, params.releaseVersion)
            break
        case 'docker-compose':
            deployDockerCompose(deployHosts, params.targetDir, params.project, params.appName, params.releaseVersion)
            break
        case 'k8s':
            deployK8s(deployHosts, params.project, params.appName, params.releaseVersion, appNs, appKind)
            break
        default:
            error("不支持的deployType: ${params.deployType}")
    }
}

private void deployStandard(List hosts, String targetDir, String project, String appName, String version) {
    echo "标准部署: $appName:$version -> $targetDir"
    
    // 将主机列表转换为逗号分隔的字符串
    def hostsStr = hosts.join(',')
    
    sh """
        # 使用ansible shell模块执行部署命令
        ansible "${hostsStr}" -i "${hostsStr},"  -m shell -a "
            mkdir -p ${targetDir} &&
            echo '${version}' > ${targetDir}/version.txt &&
            echo '[部署完成] ${appName} ${version} \$(date)' >> ${targetDir}/deploy.log &&
            ls -la ${targetDir}/
        "
    """
}

// Docker Compose部署 (使用ansible -m shell)
private void deployDockerCompose(List hosts, String targetDir, String project, String appName, String version) {
    echo "Docker Compose部署: $appName:$version -> $targetDir"
    
    def hostsStr = hosts.join(',')
    def imageTag = "192.168.5.85:8801/${project}/${appName}:${version}"
    
    sh """
        ansible "${hostsStr}" -i "${hostsStr}," -m shell -a "
            cd ${targetDir} &&
            # 更新docker-compose中的镜像标签
            sed -i.bak 's|image:.*${appName}:.*|image: ${imageTag}|g' docker-compose.yml &&
            # 拉取新镜像并重启
            docker-compose pull &&
            docker-compose up -d
        "
    """
}

// Kubernetes部署 (直接使用kubectl)
private void deployK8s(List hosts,  String project, String appName, String version, String namespace, String kind) {
    echo "K8s部署: $appName:$version -> namespace: ${namespace}"
    
    def hostsStr = hosts.join(',')
    def imageTag = "192.168.5.85:8801/${project}/${appName}:${version}"
    
    sh """
        ansible "${hostsStr}" -i "${hostsStr}," -m shell -a "
            kubectl set image ${kind}/${appName} ${appName}=${imageTag} -n ${namespace} 
        "
    """

    try {
        sh """
            ansible "${hostsStr}" -i "${hostsStr}," -m shell -a "
                kubectl rollout status ${kind}/${appName} -n ${namespace} --timeout=300s
            "
        """
        echo "发布状态检测成功"
        
    } catch (Exception e) {
        echo "发布状态检测失败: ${e.message}"
        env.ROLLBACK_NEEDED = true
        env.ERROR_MESSAGE = e.message
        currentBuild.result = 'UNSTABLE'
    }
}

// 回滚函数
def RollbackByAnsible(Map params) {
    // 检查必需参数
    if (!params.deployType) error("缺少必需参数: deployType")
    if (!params.deployHosts) error("缺少必需参数: deployHosts")
    if (!params.project) error("缺少必需参数: project")
    if (!params.appName) error("缺少必需参数: appName")
    
    // 参数处理
    def deployHosts = params.deployHosts instanceof List ? params.deployHosts : params.deployHosts.split(',')
    def appNs = params.appNs ?: 'default'
    def appKind = params.appKind ?: 'deployment'
    def rollbackVersion = params.rollbackVersion ?: 'previous'
    
    // 根据部署类型选择回滚方法
    switch(params.deployType) {
        case 'standard':
            rollbackStandard(deployHosts, params.targetDir, params.project, params.appName, rollbackVersion)
            break
        case 'docker-compose':
            rollbackDockerCompose(deployHosts, params.targetDir, params.project, params.appName, rollbackVersion)
            break
        case 'k8s':
            rollbackK8s(deployHosts, params.project, params.appName, rollbackVersion, appNs, appKind)
            break
        default:
            error("不支持的deployType: ${params.deployType}")
    }
}

// Kubernetes回滚 (直接使用kubectl)
private void rollbackK8s(List hosts,  String project, String appName, String version, String namespace, String kind) {
    echo "Kubernetes 回滚: $appName -> 版本: 上一版本"
    
    def hostsStr = hosts.join(',')

    sh """
        ansible "${hostsStr}" -i "${hostsStr}," -m shell -a "
            kubectl rollout undo ${kind}/${appName} -n ${namespace}
        "
    """
}
