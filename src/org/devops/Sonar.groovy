package org.devops

def SonarJava(projectName, projectDesc, projectVersion){
    def sonarServer = 'http://192.168.5.80:9000'
    withCredentials([string(credentialsId: '702d71be-1238-474f-9755-18728aac9c81', variable: 'AUTH_TOKEN')]){
        sh """
            sonar-scanner -Dsonar.host.url=${sonarServer} \
            -Dsonar.projectKey=${projectName} \
            -Dsonar.projectName=${projectName} \
            -Dsonar.projectVersion=${projectVersion} \
            -Dsonar.token=sqa_30d3307f2699199ecca914f87d2c2e240c59b7f0 \
            -Dsonar.ws.timeout=30 \
            -Dsonar.projectDescription=${projectDesc} \
            -Dsonar.links.homepage=http://www.baidu.com \
            -Dsonar.sources=src \
            -Dsonar.sourceEncoding=UTF-8 \
            -Dsonar.java.binaries=target/classes \
            -Dsonar.java.test.binaries=target/test-classes \
            -Dsonar.java.surefire.report=target/surefire-reports
        """
    }
}

def SonarOther(projectName, projectDesc, projectVersion){
    def sonarServer = 'http://192.168.5.80:9000'
    withCredentials([string(credentialsId: '702d71be-1238-474f-9755-18728aac9c81', variable: 'AUTH_TOKEN')]){
        sh """
            sonar-scanner -Dsonar.host.url=${sonarServer} \
            -Dsonar.projectKey=${projectName} \
            -Dsonar.projectName=${projectName} \
            -Dsonar.projectVersion=${projectVersion} \
            -Dsonar.token=sqa_30d3307f2699199ecca914f87d2c2e240c59b7f0 \
            -Dsonar.ws.timeout=30 \
            -Dsonar.projectDescription=${projectDesc} \
            -Dsonar.links.homepage=http://www.baidu.com \
            -Dsonar.sources=src \
            -Dsonar.sourceEncoding=UTF-8 
        """
    }
}

//Main
def SonarScan(type, prjectName, projectDesc, projectVersion){
    switch(type){
        case "maven":
            SonarJava(prjectName, projectDesc, projectVersion)
            break;
        default:
            SonarOther(prjectName, projectDesc, projectVersion)
            break;
    }
}