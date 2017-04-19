#!/bin/sh -ex

PREFIX="$HOME/.thrift"
mkdir -p "$PREFIX"
if [ -x "$PREFIX/bin/thrift" ]; then
	"$PREFIX/bin/thrift" --version
	exit 0
fi

BUILD="$HOME/.thrift.build"
mkdir -p "$BUILD"
cd "$BUILD"

if [ ! -d thrift-0.9.2 ]; then
	wget http://archive.apache.org/dist/thrift/0.9.2/thrift-0.9.2.tar.gz
	tar -xzf thrift-0.9.2.tar.gz
	cd thrift-0.9.2
	./configure --enable-libs=no --enable-tests=no --enable-tutorial=no --prefix="$PREFIX"
else
	cd thrift-0.9.2
fi

make -j2
make install
