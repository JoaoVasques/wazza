FROM dockerfile/java
ADD wazza-alpha.tgz /opt/wazza-alpha
WORKDIR /opt/wazza-alpha
RUN ls -l
RUN pwd
RUN wazza-alpha/bin/wazza
EXPOSE 9000
  