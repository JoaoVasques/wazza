from deployment.deploy import Deploy

import shutil
import os
from subprocess import call
import sys

class WazzaError(Exception):
    def __str__(self):
        return "Inser full path to configuration file"

class Run:

    def __init__(self, version, wazza_conf_path):
        self.version = version
        self.wazza_conf_path = wazza_conf_path
        self.tarPath = "../target/universal/wazza-" + self.version + ".tgz"
        self.output = "wazza-" + self.version
    
    def execute(self):
        print "TAR PATH: " + self.tarPath
        pwd = os.getcwd()
        if not os.path.exists(self.tarPath):
            print "tgz file does not exist. Must create it\n"
            deployDir = pwd + '/deployment'
            os.chdir(deployDir)
            Deploy(self.version).execute()
            os.chdir(pwd)

        if os.path.exists(self.output):
            shutil.rmtree(self.output)

        os.mkdir(self.output)
        os.system("tar -xvf " + self.tarPath)
        cmd = "./" + self.output + "/bin/wazza -Dconfig.file=" + self.wazza_conf_path
        print 'starting Wazza with command: ' + cmd
        os.system(cmd)
    
if len(sys.argv) > 1:
    version = "alpha"
    wazza_conf_path = sys.argv[1]
    print wazza_conf_path 
    Run(version, wazza_conf_path).execute()
else:
    raise WazzaError
