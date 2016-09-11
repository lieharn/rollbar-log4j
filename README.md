rollbar-log4j
=============
This is a library for rollbar and log4j to integrate Java apps with [Rollbar](https://rollbar.com/), the error aggregation service. You will need a Rollbar account: sign up for an account [here](https://rollbar.com/signup/).

The library is inspired by [rollbar-java] (https://github.com/rafael-munoz/rollbar-java) by Rafael Munoz and
[rollbar-maven] (https://github.com/borjafpa/rollbar-maven) by Borja Pernia

Maven will produce an OSGI-enabled JAR file.

setup
=============
Add this dependencies to your pom.xml

###### log4j appender 

    <dependency>
      <groupId>com.github.rollbar.log4j</groupId>
      <artifactId>appender</artifactId>
      <version>1.0</version>
    </dependency>
  
###### further dependencies

    <dependency>
      <groupId>log4j</groupId>
      <artifactId>log4j</artifactId>
      <version>1.2.17</version>
    </dependency>
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>javax.servlet-api</artifactId>
      <version>3.1.0</version>
    </dependency>
    <dependency>
      <groupId>org.json</groupId>
      <artifactId>json</artifactId>
      <version>20140107</version>
    </dependency>
    
###### log4j config

        <?xml version="1.0" encoding="UTF-8" ?>
        <!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
        <log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/" debug="false">
        
            <appender name="console" class="org.apache.log4j.ConsoleAppender">
                <layout class="org.apache.log4j.PatternLayout">
                    <param name="ConversionPattern" value="%d{yyyy-MM-dd HH:mm:ss} [%t] %-5p %C:%L - %m%n"/>
                </layout>
            </appender>
        
            <appender name="rollbar" class="com.github.rollbar.log4j.RollbarAppender">
                <layout class="org.apache.log4j.PatternLayout">
                    <param name="ConversionPattern" value="%d{yyyy-MM-dd HH:mm:ss} [%t] %-5p %C:%L - %m%n"/>
                </layout>
                <param name="enabled" value="true"></param>
                <param name="apiKey" value="[YOUR API KEY]"></param>
                <param name="environment" value="production"></param>
                <param name="level" value="info"></param>
            </appender>
        
            <root>
                <level value="INFO"/>
                <appender-ref ref="console"/>
                <appender-ref ref="rollbar"/>
            </root>
        </log4j:configuration>

###### pref
  
  * you need to add a preference to enable the appender
    
add this to your registry

        com.github.rollbar.log4j activated true
        
development
=============
I will add test and further configurations later. 
