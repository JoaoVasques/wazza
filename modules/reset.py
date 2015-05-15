import os
from subprocess import call

for folder in os.listdir(os.getcwd()):
  if "py" in folder:
    print "** skipping ", folder
  else:
    root = os.getcwd()
    os.chdir(folder)
    print "** working on ", folder
    os.system("ls -l")
    os.system("rm -rf app-2.10")
    os.system("rm -rf target")
    os.chdir(root)
