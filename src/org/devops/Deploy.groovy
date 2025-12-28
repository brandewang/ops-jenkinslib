package org.devops

// //ansible 发布
// def DeployByAnsible(deployHosts, targetDir, appName, releaseVersion, port){
//     //将主机写入清单文件
//     sh "rm -fr hosts"
//     for (host in "${deployHosts}".split(',')){
//         sh "echo ${host} >> hosts"
//     }
//     // sh "cat hosts"

//     // ansible 发布
//     sh """
//         # 主机连通性检测
//         ansible "${deployHosts}" -m ping -i hosts

//         # 清理和创建发布目录
//         ansible "${deployHosts}" -m shell -a "rm -fr ${targetDir}/${appName}/*" && mkdir -p ${targetDir}/${appName} || echo file is exists"

//         # 复制app
//         ansible "${deployHosts}" -m copy -a "src=${appName}-${releaseVersion}.jar dest=${targetDir}/${appName}/${appName}-${releaseVersion}.jar"

//         # 复制脚本
//         ansible "${deployHosts}" -m copy -a "src=service.sh dest=${targetDir}/${appName}/service.sh"

//         # 启动服务
//         ansible "${deployHosts}" -m shell -a "cd ${targetDir}/${appName}/ ; source /etc/profile && sh service.sh ${appName} ${releaseVersion} ${port} start"

//         # 检查服务
//         sleep 10
//         ansible "${deployHosts}" -m shell -a "cd ${targetDir}/${appName}/ ; source /etc/profile && sh service.sh ${appName} ${releaseVersion} ${port} check"
//     """

// }


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
        case 'stand':  // 改为stand表示标准/传统部署
            deployStand(deployHosts, params.targetDir, params.project, params.appName, params.releaseVersion)
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

private void deployStand(List hosts, String targetDir, String project, String appName, String version) {
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

// 2️⃣ Docker Compose部署 (使用ansible -m shell)
private void deployDockerCompose(List hosts, String targetDir, String project, String appName, String version) {
    echo "Docker Compose部署: $appName:$version -> $targetDir"
    
    def hostsStr = hosts.join(',')
    def imageTag = "prd-ops-harbor03.ciicsh.com/${project}/${appName}:${version}"
    
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

// 3️⃣ Kubernetes部署 (直接使用kubectl)
private void deployK8s(List hosts,  String project, String appName, String version, String namespace, String kind) {
    echo "K8s部署: $appName:$version -> namespace: ${namespace}"
    
    def hostsStr = hosts.join(',')
    def imageTag = "prd-ops-harbor03.ciicsh.com/${project}/${appName}:${version}"
    
    sh """
        ansible "${hostsStr}" -i "${hostsStr}," -m shell -a "
            kubectl set image ${kind}/${appName} ${appName}=${imageTag} -n ${namespace} &&
            kubectl rollout status ${kind}/${appName} -n ${namespace} --timeout=300s
        "
    """
}