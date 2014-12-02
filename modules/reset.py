import os
from subprocess import call

for folder in os.listdir(os.getcwd()):
  if "py" in folder:
    print "skip"
  else:
    root = os.getcwd()
    os.chdir(folder)
    os.system("ls -l")
    os.system("rm -rf target/*")
    os.chdir(root)
