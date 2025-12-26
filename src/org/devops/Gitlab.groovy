package org.devops

def gitlabFileOps(Map params) {
    // 根据操作类型调用不同的方法
    def operation = params.operation ?: 'download'
    
    switch(operation) {
        case 'create':
            return createFile(params)
        case 'download':
            return downloadFile(params)
        case 'upload':
        case 'update':
            return uploadFile(params)
        default:
            error "不支持的操作类型: ${operation}"
    }
}

/**
 * 创建文件
 */
def createFile(Map params) {
    // 必要参数
    def gitlabUrl = params.gitlabUrl ?: env.GITLAB_URL ?: 'https://gitlab.com'
    def projectId = params.projectId
    def filePath = params.filePath
    def content = params.content
    def token = params.token ?: env.GITLAB_TOKEN
    def branch = params.branch ?: 'main'
    def commitMsg = params.commitMsg ?: "Create file via Jenkins"
    
    // 参数检查
    if (!projectId || !filePath || !content || !token) {
        error "缺少必要参数: projectId, filePath, content, token"
    }
    
    // API 地址
    def apiUrl = "${gitlabUrl}/api/v4/projects/${URLEncoder.encode(projectId.toString(), 'UTF-8')}/repository/files/${URLEncoder.encode(filePath, 'UTF-8')}"
    
    // 请求体
    def body = [
        branch: branch,
        content: Base64.getEncoder().encodeToString(content.getBytes('UTF-8')),
        commit_message: commitMsg,
        encoding: 'base64'
    ]
    
    // HTTP 请求
    def response = httpRequest(
        url: apiUrl,
        method: 'POST',
        contentType: 'APPLICATION_JSON',
        customHeaders: [[name: 'PRIVATE-TOKEN', value: token]],
        requestBody: new groovy.json.JsonBuilder(body).toString(),
        consoleLogResponseBody: false
    )
    
    if (response.status == 201) {
        echo "✅ 文件创建成功: ${filePath}"
        return true
    } else {
        error "❌ 文件创建失败: HTTP ${response.status} - ${response.content}"
    }
}

/**
 * 下载文件
 */
def downloadFile(Map params) {
    // 必要参数
    def gitlabUrl = params.gitlabUrl ?: env.GITLAB_URL ?: 'https://gitlab.com'
    def projectId = params.projectId
    def filePath = params.filePath
    def token = params.token ?: env.GITLAB_TOKEN
    def branch = params.branch ?: 'main'
    def savePath = params.savePath // 可选保存路径
    
    // 参数检查
    if (!projectId || !filePath || !token) {
        error "缺少必要参数: projectId, filePath, token"
    }
    
    // API 地址
    def encodedPath = URLEncoder.encode(filePath, 'UTF-8')
    def apiUrl = "${gitlabUrl}/api/v4/projects/${URLEncoder.encode(projectId.toString(), 'UTF-8')}/repository/files/${encodedPath}/raw?ref=${branch}"
    
    // HTTP 请求
    def response = httpRequest(
        url: apiUrl,
        method: 'GET',
        customHeaders: [[name: 'PRIVATE-TOKEN', value: token]],
        consoleLogResponseBody: false
    )
    
    if (response.status == 200) {
        def content = response.content
        
        // 如果指定了保存路径，保存文件
        if (savePath) {
            writeFile(file: savePath, text: content)
            echo "✅ 文件下载并保存到: ${savePath}"
        } else {
            echo "✅ 文件下载成功: ${filePath}"
        }
        
        return content
    } else {
        error "❌ 文件下载失败: HTTP ${response.status}"
    }
}

/**
 * 上传/更新文件
 */
def uploadFile(Map params) {
    // 必要参数
    def gitlabUrl = params.gitlabUrl ?: env.GITLAB_URL ?: 'https://gitlab.com'
    def projectId = params.projectId
    def filePath = params.filePath
    def token = params.token ?: env.GITLAB_TOKEN
    def branch = params.branch ?: 'main'
    def commitMsg = params.commitMsg ?: "Update file via Jenkins"
    
    // 内容来源：直接内容或本地文件
    def content
    if (params.content) {
        content = params.content
    } else if (params.localFile) {
        content = readFile(params.localFile)
    } else {
        error "需要提供 content 或 localFile 参数"
    }
    
    // 参数检查
    if (!projectId || !filePath || !token) {
        error "缺少必要参数: projectId, filePath, token"
    }
    
    // API 地址
    def apiUrl = "${gitlabUrl}/api/v4/projects/${URLEncoder.encode(projectId.toString(), 'UTF-8')}/repository/files/${URLEncoder.encode(filePath, 'UTF-8')}"
    
    // 请求体
    def body = [
        branch: branch,
        content: Base64.getEncoder().encodeToString(content.getBytes('UTF-8')),
        commit_message: commitMsg,
        encoding: 'base64'
    ]
    
    // HTTP 请求 (使用 PUT 更新文件)
    def response = httpRequest(
        url: apiUrl,
        method: 'PUT',
        contentType: 'APPLICATION_JSON',
        customHeaders: [[name: 'PRIVATE-TOKEN', value: token]],
        requestBody: new groovy.json.JsonBuilder(body).toString(),
        consoleLogResponseBody: false
    )
    
    if (response.status == 200) {
        echo "✅ 文件上传成功: ${filePath}"
        return true
    } else {
        error "❌ 文件上传失败: HTTP ${response.status} - ${response.content}"
    }
}

// 方法1: 使用 call 方法 + operation 参数
// gitlabFileOperations(
//     operation: 'create',
//     projectId: projectId,
//     filePath: 'config/test.txt',
//     content: 'Hello GitLab',
//     token: GITLAB_TOKEN
// )

// // 方法2: 直接调用具体方法
// gitlabFileOperations.create(
//     projectId: projectId,
//     filePath: 'config/settings.json',
//     content: '{"env": "production"}',
//     token: GITLAB_TOKEN
// )

// // 下载文件
// def content = gitlabFileOperations.download(
//     projectId: projectId,
//     filePath: 'README.md',
//     token: GITLAB_TOKEN,
//     savePath: 'downloaded.md'
// )

// // 上传文件
// gitlabFileOperations.upload(
//     projectId: projectId,
//     filePath: 'config/settings.json',
//     content: '{"env": "staging"}',
//     token: GITLAB_TOKEN
// )