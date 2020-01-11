FROM maven:3.3-jdk-8

COPY start.sh /
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /

RUN chmod a+x /start.sh && \
    chmod a+x /wait-for-it.sh

COPY /* /DragonRaidSched/
RUN cd DragonRaidSched && \
    mvn install

