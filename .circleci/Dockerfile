FROM ubuntu:22.04

RUN apt-get update -y
#&& apt-get upgrade -y

RUN apt-get install build-essential -y --fix-missing

RUN echo "mysql-server mysql-server/root_password password root" | debconf-set-selections

RUN echo "mysql-server mysql-server/root_password_again password root" | debconf-set-selections

RUN apt install mysql-server -y

RUN usermod -d /var/lib/mysql/ mysql

RUN [ -d /var/run/mysqld ] || mkdir -p /var/run/mysqld

ADD ./runMySQL.sh /runMySQL.sh

RUN chmod +x /runMySQL.sh

RUN apt-get install -y git-core

RUN apt-get install -y wget

RUN mkdir /usr/java

RUN apt-get install jq -y

RUN apt-get install curl -y

RUN apt-get install unzip -y

# Install OpenJDK 21.0.7
RUN wget https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.7%2B6/OpenJDK21U-jdk_x64_linux_hotspot_21.0.7_6.tar.gz

RUN mv OpenJDK21U-jdk_x64_linux_hotspot_21.0.7_6.tar.gz /usr/java
RUN cd /usr/java && tar -xzvf OpenJDK21U-jdk_x64_linux_hotspot_21.0.7_6.tar.gz -C /usr/java/
RUN mv /usr/java/jdk-21.0.7+6 /usr/java/jdk-21.0.7
RUN echo 'JAVA_HOME=/usr/java/jdk-21.0.7' >> /etc/profile
RUN echo 'JRE_HOME=/usr/java/jdk-21.0.7' >> /etc/profile
RUN echo 'PATH=$PATH:$HOME/bin:$JAVA_HOME/bin' >> /etc/profile
RUN echo 'export JAVA_HOME' >> /etc/profile
RUN echo 'export JRE_HOME' >> /etc/profile
RUN echo 'export PATH' >> /etc/profile

RUN update-alternatives --install "/usr/bin/java" "java" "/usr/java/jdk-21.0.7/bin/java" 1
RUN update-alternatives --install "/usr/bin/javac" "javac" "/usr/java/jdk-21.0.7/bin/javac" 1

#install postgres 13
# Import Repository Signing Key
RUN wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -
RUN DEBIAN_FRONTEND=noninteractive TZ=Etc/UTC apt-get -y install tzdata
RUN apt install curl gpg gnupg2 software-properties-common apt-transport-https lsb-release ca-certificates sudo -y
# Add PostgreSQL repository
RUN echo "deb http://apt.postgresql.org/pub/repos/apt/ `lsb_release -cs`-pgdg main" | tee  /etc/apt/sources.list.d/pgdg.list
# Update again
RUN apt update
# Install PostgreSQL 13
RUN apt install -y postgresql-13
#  Verify PostgreSQL 13 Installation on Ubuntu 22.04|20.04|18.04
RUN psql --version
# Manage PostgreSQL 13 service
#you can manage with `service postgresql start`