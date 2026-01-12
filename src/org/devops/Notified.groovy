package org.devops

def SendEmail(userEmail){
    def errorHtml = env.ERROR_MESSAGE ? "<p><strong>错误信息:</strong> ${env.ERROR_MESSAGE}</p>" : ""
    def opsEmail = 'brande.wang@hotmail.com'
    def userList = userEmail.split('\\s+')
    def opsList = opsEmail.split('\\s+')
    def allEmails = (userList + opsList).toList().unique().join(' ')

    emailext(
                subject: "构建通知: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                to: allEmails,
                body: """
                <html>
                <body>
                    <p><strong>项目:</strong> ${env.JOB_NAME}</p>
                    <p><strong>构建号:</strong> #${env.BUILD_NUMBER}</p>
                    <p><strong>状态:</strong> <span style="color: ${currentBuild.currentResult == 'SUCCESS' ? 'green' : 'red'}; font-weight: bold;">${currentBuild.currentResult}</span></p>
                    <p><strong>时间:</strong> ${new Date().format('yyyy-MM-dd HH:mm:ss')}</p>
                    <p><strong>详情:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    <p><strong>触发原因:</strong> ${env.triggerDescription}</p>
                    ${errorHtml}
                    <hr>
                    <p style="color: gray; font-size: 12px;">Jenkins 自动通知</p>
                </body>
                </html>
                """,
                mimeType: 'text/html'
    )
}