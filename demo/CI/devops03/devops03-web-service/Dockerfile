FROM 192.168.5.85:8801/library/nginx:1.28.1

RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo "Asia/Shanghai" > /etc/timezone

RUN rm /etc/nginx/conf.d/default.conf
COPY default.conf /etc/nginx/conf.d/
COPY dist/ /usr/share/nginx/html/

EXPOSE 80