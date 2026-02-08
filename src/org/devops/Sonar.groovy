package org.devops

def SonarJava(projectName, projectDesc, projectVersion, homePage, branchName){
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
            -Dsonar.branch.name=${branchName} \
            -Dsonar.java.binaries=target/classes \
            -Dsonar.java.test.binaries=target/test-classes \
            -Dsonar.java.surefire.report=target/surefire-reports
        """
    }
}

def SonarOther(projectName, projectDesc, projectVersion, homePage, branchName){
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
            -Dsonar.branch.name=${branchName}
        """
    }
}

def SonarQualityGate(){
    def qg = waitForQualityGate()
    if (qg.status != 'OK') {
        env.ERROR_MESSAGE = "Pipeline aborted due to quality gate failure: ${qg.status}"
        error "${ERROR_MESSAGE}"
    }
}

//Main
def SonarScan(type, prjectName, projectDesc, projectVersion, homePage, branchName){
    switch(type){
        case "maven":
            SonarJava(prjectName, projectDesc, projectVersion, homePage, branchName)
            break;
        default:
            SonarOther(prjectName, projectDesc, projectVersion, homePage, branchName)
            break;
    }
}