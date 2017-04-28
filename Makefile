.PHONY : crossdock crossdock-fresh clean coverage dockclean install test build harness release

.DEFAULT_GOAL : help

help:  ## Show this help.
	@fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//' | sed -e 's/##//'

crossdock: dockclean
	docker-compose build
	docker-compose run crossdock

crossdock-fresh: dockclean build
	docker-compose pull
	docker-compose build
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

build:
	./gradlew build --stacktrace

harness:  # Run harness crossdock server locally without docker.
	java -jar crossdock-java/build/libs/crossdock-0.0.1-SNAPSHOT.jar

release: clean build test
	@echo "please make sure you are using java 7."
	@read -p "Press any key to continue, or press Control+C to cancel. " x;
	./gradlew uploadArchives
