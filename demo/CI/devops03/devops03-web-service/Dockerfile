FROM 192.168.5.85:8801/library/nginx:1.28.1
# 删除nginx 默认配置
RUN rm /etc/nginx/conf.d/default.conf

# 添加我们自己的配置 default.conf 在下面
COPY default.conf /etc/nginx/conf.d/

# 把生成dist文件夹下的文件copy到nginx下面去
COPY dist/ /usr/share/nginx/html/

EXPOSE 80