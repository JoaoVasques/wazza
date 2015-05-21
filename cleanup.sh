#!/bin/sh

activator clean
rm -rf target
rm -rf project/target
rm -rf project/project
rm -rf app-2.11
cd modules
python reset.py
