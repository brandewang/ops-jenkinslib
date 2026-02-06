package org.devops

def SonarJava(projectName, projectDesc, projectVersion, homePage){
    withSonarQubeEnv("sonarqube-server"){
        sh """
            sonar-scanner -Dsonar.projectKey=${projectName} \
            -Dsonar.projectName=${projectName} \
            -Dsonar.projectVersion=${projectVersion} \
            -Dsonar.scanner.socketTimeout=30 \
            -Dsonar.projectDescription=${projectDesc} \
            -Dsonar.links.homepage=${homePage} \
            -Dsonar.sources=src \
            -Dsonar.sourceEncoding=UTF-8 \
            -Dsonar.java.binaries=target/classes \
            -Dsonar.java.test.binaries=target/test-classes \
            -Dsonar.java.surefire.report=target/surefire-reports
        """
    }
    def qg = waitForQualityGate()
    if (qg.status != 'OK') {
        error "Pipeline aborted due to quality gate failure: ${qg.status}"
    }
}

def SonarOther(projectName, projectDesc, projectVersion, homePage){
    withSonarQubeEnv("sonarqube-server"){
        sh """
            sonar-scanner -Dsonar.projectKey=${projectName} \
            -Dsonar.projectName=${projectName} \
            -Dsonar.projectVersion=${projectVersion} \
            -Dsonar.scanner.socketTimeout=30 \
            -Dsonar.projectDescription=${projectDesc} \
            -Dsonar.links.homepage=${homePage} \
            -Dsonar.sources=src \
            -Dsonar.sourceEncoding=UTF-8 
        """
    }
    def qg = waitForQualityGate()
    if (qg.status != 'OK') {
        error "Pipeline aborted due to quality gate failure: ${qg.status}"
    }
}

//Main
def SonarScan(type, prjectName, projectDesc, projectVersion, homePage){
    switch(type){
        case "maven":
            SonarJava(prjectName, projectDesc, projectVersion, homePage)
            break;
        default:
            SonarOther(prjectName, projectDesc, projectVersion, homePage)
            break;
    }
}