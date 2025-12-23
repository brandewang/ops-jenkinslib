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
        case "npm":
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

def DockerBuild(harbor_url, image_project, image_repo, image_tag){
    sh """
        #登录镜像仓库
        docker login ${DEFAULT_HARBOR_URL} -u admin -p 7F#SanTGqG6E

        #构建镜像
        docker build -t ${DEFAULT_HARBOR_URL}/${app.image_project}/${app.image_repo}:${env.IMAGE_TAG} .

        #上传镜像
        docker push ${DEFAULT_HARBOR_URL}/${app.image_project}/${app.image_repo}:${env.IMAGE_TAG}

        #删除镜像
        sleep 2
        docker rmi ${DEFAULT_HARBOR_URL}/${app.image_project}/${app.image_repo}:${env.IMAGE_TAG}
    """
}
