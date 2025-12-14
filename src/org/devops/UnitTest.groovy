package org.devops

//Maven
def MavenTest(){
    sh "mvn test -s settings.xml"
    junit 'target/surefire-reports/*.xml'
}

//Gradle
def GradleTest(){
    sh "gradle test"
    junit 'build/test-results/test/*.xml'
}

//Golang
def GoTest(){
    sh "go test"
}

//Npm
def Npmtest(){
    sh "npm test"
}

//Yarm
def YarnTest(){
    sh "yarn test"
}

//Main
def CodeTest(type){
    switch(type){
        case "maven":
            MavenTest()
            break;
        case "gradle":
            GradleTest()
            break;
        default:
            error: "No such tools ... [maven/ant/gradle/npm/yarm/go]"
            break;
    }
}