package org.devops

def SonarJava(projectName, groupName){
    withCredentials([string(credentialsId: '', variable: 'AUTH_TOKEN')]){
        sh """
            sonar-scanner -Dsonar.host.url=http://192.168.0.1:9000 \
                -Dsonar.projectKey=${projectName} \
                -Dsonar.projectName=${projectName} \
                -Dsonar.projectVersion=${BUILD_ID} \
                -Dsonar.login=${AUTH_TOKEN} \
                -Dsonar.ws.timeout=30 \
                -Dsonar.projectDescription="my first project!" \
                -Dsonar.links.homepage="" \
                -Dsonar.links.ci="" \
                -Dsonar.sources=src \
                -Dsonar.sourceEncoding=UTF-8 \
                -Dsonar.java.binaries=target/classes \
                -Dsonar.java.test.binaries=target/test-classes \
                -Dsonar.java.surefire.report=target/surefire-reports
        """
    }
}

def Sonar(projectName, groupName){
    withCredentials([string(credentialsId: '', variable: 'AUTH_TOKEN')]){
        sh """
            sonar-scanner -Dsonar.host.url=http://192.168.0.1:9000 \
                -Dsonar.projectKey=${projectName} \
                -Dsonar.projectName=${projectName} \
                -Dsonar.projectVersion=${BUILD_ID} \
                -Dsonar.login=${AUTH_TOKEN} \
                -Dsonar.ws.timeout=30 \
                -Dsonar.projectDescription="my first project!" \
                -Dsonar.links.homepage="" \
                -Dsonar.links.ci="" \
                -Dsonar.sources=src \
                -Dsonar.sourceEncoding=UTF-8
        """
    }
}