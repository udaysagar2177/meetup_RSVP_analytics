FROM ubuntu:14.04
MAINTAINER uday sagar shiramshetty <udaysagar.2177@gmail.com>

RUN apt-get install -y wget git vim curl initscripts net-tools passwd sudo tar
RUN apt-get -y install mysql-server libapache2-mod-auth-mysql php5-mysql
ADD run.sh /opt/

CMD ./opt/run.sh
