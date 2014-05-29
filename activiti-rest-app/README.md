# Activiti Rest custom installation

## Setup

configure your database settings in *src/main/resources/activiti.cfg.xml* and execute the following sql script

	insert into ACT_ID_GROUP values ('admin', 1, 'System administrator', 'security-role');
	insert into ACT_ID_USER values ('admin', 1, 'admin', 'Administrator', 'admin@cnr.it', 'admin');
	insert into ACT_ID_MEMBERSHIP values ('admin', 'admin');


## Run the application

	mvn clean tomcat7:run-war


## Sample usage

	curl http://localhost:8280/activiti-rest-custom/service/management/table/ACT_ID_INFO -uadmin:admin  | jsonlint
