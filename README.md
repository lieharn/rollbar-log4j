rollbar-log4j
=============
This is a library for rollbar and log4j to integrate Java apps with [Rollbar](https://rollbar.com/), the error aggregation service. You will need a Rollbar account: sign up for an account [here](https://rollbar.com/signup/).

The library is inspired by [rollbar-java] (https://github.com/rafael-munoz/rollbar-java) by Rafael Munoz and
[rollbar-maven] (https://github.com/borjafpa/rollbar-maven) by Borja Pernia


setup
=============
Add this dependencies to your pom.xml

log4j appender 

    <dependency>
      <groupId>com.github.rollbar.log4j</groupId>
      <artifactId>appender</artifactId>
      <version>1.0</version>
    </dependency>
  
further dependencies

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
    
log4j config


  
  
