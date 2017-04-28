#!/bin/sh -ex

PREFIX="$HOME/.thrift"
VERSION="0.9.2"
mkdir -p "$PREFIX"
if [ -x "$PREFIX/bin/thrift" ]; then
	"$PREFIX/bin/thrift" --version
	exit 0
fi

BUILD="$HOME/.thrift.build"
mkdir -p "$BUILD"
cd "$BUILD"

if [ ! -d thrift-"$VERSION" ]; then
	wget http://archive.apache.org/dist/thrift/"$VERSION"/thrift-"$VERSION".tar.gz
	tar -xzf thrift-"$VERSION".tar.gz
	cd thrift-"$VERSION"
	./configure --enable-libs=no --enable-tests=no --enable-tutorial=no --prefix="$PREFIX"
else
	cd thrift-"$VERSION"
fi

make -j2
make install
