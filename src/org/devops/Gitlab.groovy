package org.devops

// 封装GitlabHttpReq
def GitlabHttpReq(reqType, reqUrl, reqbody) {
    def gitServer = "http://192.168.5.85:8802/api/v4"
    withCredentials([string(credentialsId: 'c9ab4fde-97c2-454c-8886-7be8a9fd3479', variable: 'GITLAB_TOKEN')]) {
        def fullUrl = "${gitServer}/${reqUrl}"
        
        // 修正 requestBody 处理，如果是空字符串则设为 null
        def requestBodyContent = reqbody ?: null
        
        def response = httpRequest(
            acceptType: 'APPLICATION_JSON_UTF8',
            consoleLogResponseBody: true,
            contentType: 'APPLICATION_JSON_UTF8',
            customHeaders: [[maskValue: true, name: 'PRIVATE-TOKEN', value: env.GITLAB_TOKEN]],
            httpMode: "${reqType}",
            url: fullUrl,
            wrapAsMultipart: false,
            requestBody: requestBodyContent
        )
        
        return response.content
    }
}

def GetProjectId(groupName, projectName){
    // 修正字符串插值方式
    def apiUrl = "projects?search=${projectName}"
    def responseContent = GitlabHttpReq('GET', apiUrl, "")
    
    // 修正变量名拼写错误
    def response = readJSON text: responseContent
    
    // 修正逻辑：遍历所有项目，找到匹配的才返回
    for (item in response) {
        if (item["path_with_namespace"] == "${groupName}/${projectName}") {
            return item["id"]
        }
    }
    
    // 如果没找到，返回 0 或抛出异常
    echo "项目 ${groupName}/${projectName} 未找到"
    return 0
}