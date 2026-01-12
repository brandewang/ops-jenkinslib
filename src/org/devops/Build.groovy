package org.devops

//Maven
def MavenBuild(module){
    if(module){
        sh "mvn clean package -DskipTests -s settings.xml -pl ${module} -am"
       
    }else {
        sh "mvn clean package -DskipTests -s settings.xml"
    }
}

//Gradle
def GradleBuild(){
    sh "gradle build -x test"
}

//Ant
def AntBuild(configPath="./build.xml"){
    sh "ant -f ${configPath}"
}

//Golang
def GoBuild(){
    sh "go build demo.go"
}

//Npm
def NpmBuild(){
    sh "npm cache clean --force"
    sh "npm config set registry http://192.168.5.85:8803/repository/npm-proxy/"
    sh "npm install && npm run build"
}

//Yarm
def YarnBuild(){
    sh "yarn install && yarn build"
}

//Main
def CodeBuild(type, module='', configPath=""){
    switch(type){
        case "maven":
            MavenBuild(module)
            break;
        case "gradle":
            GradleBuild()
            break;
        case "node14":
            NpmBuild()
            break;
        case "yarm":
            YarnBuild()
            break;
        default:
            error: "No such tools ... [maven/ant/gradle/npm/yarm/go]"
            break;
    }
}


