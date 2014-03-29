# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  # Every Vagrant virtual environment requires a box to build off of.
  config.vm.box = "wazza"
  config.vm.box_url = "https://dl.dropboxusercontent.com/u/5858910/package.box"

  # The ruby runtime is also installed; just change the hostname to debian7rc1 to ensure puppet is executed with rvm also
  #config.vm.hostname = "wazza"

  config.vm.network :forwarded_port, guest: 3000, host: 3000
  config.vm.network :private_network, ip: "172.16.16.16"

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:

  config.vm.provider :virtualbox do |vb|
    # Don't boot with headless mode
    vb.gui = false

    # Use VBoxManage to customize the VM. For example to change memory
    vb.customize ["modifyvm", :id, "--memory", "1024", "--cpus", "2"]

    #Fix Symlinks in Virtualbox Shared Folders
    vb.customize ["setextradata", :id, "VBoxInternal2/SharedFoldersEnableSymlinksCreate/v-root", "1"]
  end

  # config.vm.synced_folder "bootstrap/puppet/files", "/etc/puppet/files"
  #config.vm.provision :puppet do |puppet|
  #  puppet.options = "--fileserverconfig=/vagrant/bootstrap/puppet/fileserver.conf"
  #  puppet.manifests_path = "bootstrap/puppet/manifests"
  #  puppet.module_path = "bootstrap/puppet/modules"
  #  puppet.manifest_file  = "site.pp"
  #end

  #config.vm.provision :shell, :inline => "/bin/bash /vagrant/bootstrap/puppet/update.sh"

  #config.vm.synced_folder "bootstrap/karma-mobile-reporter", "/home/vagrant/karma-mobile-reporter"
  #config.vm.synced_folder "./", "/home"
end