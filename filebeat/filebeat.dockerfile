FROM docker.elastic.co/beats/filebeat:7.17.18

USER root

COPY filebeat.yml /usr/share/filebeat/filebeat.yml

RUN chmod go-w /usr/share/filebeat/filebeat.yml