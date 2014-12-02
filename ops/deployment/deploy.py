import os
from subprocess import call
import sys

class WazzaDeployemntError(Exception):
    def __str__(self):
        return "Insert version"

class Deploy:

    def __init__(self, version):
        self.version = version
    
    def generate_zip_package(self):
        os.system("activator clean")
        os.system("activator compile")
        os.system("activator universal:packageZipTarball")

    def create_beanstalk_zip(self):
        os.system("zip --junk-paths ../../target/aws.zip Dockerfile ../../target/universal/wazza-alpha.tgz")

    def create_ec2_zip(self):
        os.system("zip --junk-paths deploy.zip ../../conf/prod.conf ../../target/universal/wazza-alpha.tgz")

    def execute(self):
        pwd = os.getcwd()
        os.chdir(os.path.dirname(os.path.dirname(pwd)))
        os.system("git checkout master")
        os.system("git pull origin master")
        self.generate_zip_package()
        os.chdir(pwd)
        self.create_ec2_zip()
        self.create_beanstalk_zip()
        
        

if len(sys.argv) > 1:
    version = sys.argv[1]
    Deploy(version).execute()
else:
    raise WazzaDeployemntError  
