### 系统运维，自动发送邮件提醒服务
#### 系统架构springboot2.6.1+hutool+java.mail
#### JDK版本openJDK11
#### 打包方式
##### 直接通过maven打包
1. mvn clean package （jar文件较大）
2. 启动命令java -jar xxx.jar
##### 通过插件打包  
1. pom文件中关键配置如下  
```
    <layout>ZIP</layout>  
    <includes>  
        <include>  
            <groupId>nothing</groupId>  
            <artifactId>nothing</artifactId>  
        </include>  
    </includes>  
```  
2. 将直接打包后的jar文件中BOOT-INF下的lib目录复制
3. 然后再次打包mvn clean package（jar文件很小了）
4. 将复制后的lib目录和打包后的jar文件放到同一个目录中
5. 启动命令java -Dloader.path="lib/" -jar xxx.jar
##### jar启动读取外部配置文件
1. 在启动脚本所在目录创建config目录，在该目录下创建application.properties文件，里面变量配置信息
2. 这样就会覆盖默认的配置文件，达到不用修改代码就可以灵活修改配置信息
3. 当然也可以直接把jar中配置文件放到jar包的同级目录
4. 因为src/main/resources 文件夹下创建的application.properties 文件的优先级是最低的

