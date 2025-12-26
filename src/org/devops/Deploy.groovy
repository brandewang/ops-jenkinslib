package org.devops

//ansible 发布
def DeployByAnsible(deployHosts, targetDir, appName, releaseVersion, port){
    //将主机写入清单文件
    sh "rm -fr hosts"
    for (host in "${deployHosts}".split(',')){
        sh "echo ${host} >> hosts"
    }
    // sh "cat hosts"

    // ansible 发布
    sh """
        # 主机连通性检测
        ansible "${deployHosts}" -m ping -i hosts

        # 清理和创建发布目录
        ansible "${deployHosts}" -m shell -a "rm -fr ${targetDir}/${appName}/*" && mkdir -p ${targetDir}/${appName} || echo file is exists"

        # 复制app
        ansible "${deployHosts}" -m copy -a "src=${appName}-${releaseVersion}.jar dest=${targetDir}/${appName}/${appName}-${releaseVersion}.jar"

        # 复制脚本
        ansible "${deployHosts}" -m copy -a "src=service.sh dest=${targetDir}/${appName}/service.sh"

        # 启动服务
        ansible "${deployHosts}" -m shell -a "cd ${targetDir}/${appName}/ ; source /etc/profile && sh service.sh ${appName} ${releaseVersion} ${port} start"

        # 检查服务
        sleep 10
        ansible "${deployHosts}" -m shell -a "cd ${targetDir}/${appName}/ ; source /etc/profile && sh service.sh ${appName} ${releaseVersion} ${port} check"
    """

}