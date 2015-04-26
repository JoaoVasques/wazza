<h1>Reverse proxy for Wazza</h1>

The goal of using [nginx](http://nginx.org) is to add an additional layer of security and load balacing. Currently only security is supported.

This is document will guide you through the setup of nginx

Download *nginx* and [naxsi](https://github.com/nbs-system/naxsi)

    wget http://nginx.org/download/nginx-1.8.0.tar.gz
    tar -xvf nginx-1.8.0.tar.gz
    wget https://github.com/nbs-system/naxsi/archive/master.zip
    unzip master.zip

Compile and install *nginx* with *naxsi* as a module

    cd nginx-1.8.0
    ./configure --add-module=../naxsi-master/naxsi_src/ --with-http_ssl_module
    make
    make install


We're almost done! We only need to replace some configuration files.

    cp ${WAZZA_HOME}/ops/nginx/nginx.conf ${NGINX_HOME}/conf
    cp naxsi-master/naxsi_config/naxsi_core.rules ${NGINX_HOME}/conf

Start *nginx* and open browser

    nginx
    http://localhost:8080

