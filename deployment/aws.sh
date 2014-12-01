#!/bin/sh

cd ..
git checkout master
git pull origin master
activator clean
activator compile
activator universal:packageZipTarball
cd -

zip --junk-paths ../target/aws.zip Dockerfile ../target/universal/wazza-alpha.tgz
