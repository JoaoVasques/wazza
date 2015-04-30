<h1>Run NewRelic</h1>

To run Wazza using *New Relic* (on production mode only)

    activator clean dist && unzip target/universal/*.zip
    cd ${UNZIPED}; ./bin/wazza-${VERSION} -J-javaagent:/${WAZZA_HOME}/conf/newrelic/newrelic.jar -newrelic.config.file=${WAZZA_HOME}/conf/newrelic/newrelic.yml
    
