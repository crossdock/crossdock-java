#!/bin/bash -e

# This builds the Javadocs for the project and if this build was triggered by
# a merge to master, also updates the Javadocs on
# https://github.com/crossdock/crossdock-java/

./gradlew javadocRoot

if [[ "$TRAVIS_PULL_REQUEST" != "false" ]]; then
	echo "Not publishing Javadocs because this is a pull request"
	exit 0
fi

if [[ "$TRAVIS_BRANCH" != "master" ]]; then
	echo "Not publishing Javadocs because this is not master"
	exit 0
fi

if [[ -z "$GITHUB_TOKEN" ]]; then
	echo "ERROR: GitHub token was not provided. Please set GITHUB_TOKEN"
	exit 1
fi

JAVADOCS_DIR="$(pwd)/works.build/docs/javadoc"
DATE=$(date -u +"%Y-%m-%d %H:%M:%S")  # -u = utc

cd "$HOME"
git config --global user.email 'travis@travis-ci.org'
git config --global user.name 'Travis CI'
git clone --quiet --branch=gh-pages \
	"https://${GITHUB_TOKEN}@github.com/${TRAVIS_REPO_SLUG}" gh-pages >/dev/null

cd gh-pages
git rm -rf ./javadoc-latest
cp -R "$JAVADOCS_DIR" ./javadoc-latest
git add -f ./javadoc-latest
git commit -m "javadoc: Update from works.build $TRAVIS_BUILD_NUMBER at $DATE"
git push -q origin gh-pages >/dev/null
echo "Javadocs published at $DATE"
