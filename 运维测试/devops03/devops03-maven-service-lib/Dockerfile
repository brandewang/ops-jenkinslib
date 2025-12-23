FROM harbor.ciicsh.com/library/openjdk:17-sw
MAINTAINER brandewang<brandewang@hotmail.com>
WORKDIR /home
ENV APP_NAME="demo1.jar"
ENV JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC"
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo "Asia/Shanghai" > /etc/timezone
COPY target/${APP_NAME} /home
ENTRYPOINT java ${JAVA_OPTS} -Dfile.encoding=utf-8 -Djava.security.egd=file:/dev/./urandom -jar /home/${APP_NAME}
