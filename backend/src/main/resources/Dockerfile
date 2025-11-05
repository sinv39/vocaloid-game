FROM eclipse-temurin:17-jre-jammy

# 设置工作目录
WORKDIR /app

# 复制jar包到容器中
COPY vocaloid-game-1.0.0.jar vocaloid-game-1.0.0.jar

# 暴露端口（根据你的应用端口修改）
EXPOSE 10001

# 设置JVM参数和启动命令
ENTRYPOINT ["java", "-jar", "vocaloid-game-1.0.0.jar"]