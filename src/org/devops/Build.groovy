package org.devops

//Maven
def MavenBuild(settingsPath="./settings.xml"){
    sh "mvn clean package -DskipTests -s ${settingsPath}"
}

//Gradle
def GradleBUild(){
    sh "gradle build -x test"
}

//Golang
def GoBuild(){
    sh "go build demo.go"
}

//Npm
def NpmBuild(){
    sh "npm install && npm run build"
}

//Yarm
def YarnBuild(){
    sh "yarn install && yarn build"
}

//Main
def CodeBuild(type, configPath=""){
    switch(type){
        case "maven":
            MavenBuild(settingsPath="./settings.xml")
            break;
        case "gradle":
            GradleBUild()
            break;
        default:
            error: "No such tools ... [maven/ant/gradle/npm/yarm/go]"
            break;
    }
}