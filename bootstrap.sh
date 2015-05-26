#!/usr/bin/env bash

export DEBIAN_FRONTEND=noninteractive

# All the commands will run as the root by ubuntu provisioning
# unless the user is explicitly set.
# Add '/usr/local/bin' to root's $PATH variable.
export PATH=$PATH:/usr/local/bin

# Update
apt-get -q -y autoclean
apt-get -q -y autoremove
apt-get -q -y update
apt-get -q -y upgrade

# Tools
apt-get -q -y install vim bzip2 curl zip git

# NFS Client
apt-get -q -y install nfs-common

# Java
apt-get -q -y install openjdk-8-jdk

# Play
su - ubuntu -c "wget http://downloads.typesafe.com/typesafe-activator/1.3.2/typesafe-activator-1.3.2.zip"
su - ubuntu -c "rm -rf activator-1.3.2"
su - ubuntu -c "unzip typesafe-activator-1.3.2.zip"
su - ubuntu -c "rm typesafe-activator-1.3.2.zip"

# Redis
apt-get -q -y install redis-server

# .bash_profile
su - ubuntu -c "echo 'source ~/.profile' > .bash_profile"
su - ubuntu -c "echo 'export PATH=$PATH:~/activator-1.3.2' >> ~/.bash_profile"
su - ubuntu -c "echo 'export SBT_OPTS=\"-Xmx2000M -Xss2M -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled\"' >> ~/.bash_profile"
su - ubuntu -c "source ~/.bash_profile"
