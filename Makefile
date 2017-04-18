.PHONY : crossdock crossdock-fresh clean coverage dockclean install test works.build harness release

.DEFAULT_GOAL : help

help:  ## Show this help.
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'

crossdock: dockclean
	docker-compose works.build java
	docker-compose run crossdock

crossdock-fresh: dockclean works.build
	docker-compose pull
	docker-compose works.build
	docker-compose run crossdock

clean:
	./gradlew clean

coverage:
	./gradlew jacocoRootReport

dockclean:
	docker-compose kill
	docker-compose rm --force

install:  ## Install project dependencies.
	./gradlew install

test:
	./gradlew check

works.build:
	./gradlew works.build --stacktrace

harness:  # Run harness crossdock server locally without docker.
	java -jar yarpc-crossdock/works.build/libs/yarpc-crossdock-jar-with-dependencies.jar

release: clean works.build test
	@echo "please make sure you are using java 7."
	@read -p "Press any key to continue, or press Control+C to cancel. " x;
	./gradlew uploadArchives
