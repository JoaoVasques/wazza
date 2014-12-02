# Wazza

## Setup and Run

Install activator and put it in your system's PATH
    ```
    wget http://downloads.typesafe.com/typesafe-activator/1.2.10/typesafe-activator-1.2.10-minimal.zip
    unzip typesafe-activator-1.2.10-minimal.zip
    export PATH="${PATH_TO_ACTIVATOR}/activator-1.2.10-minimal/:$PATH"
    ```
Clone the repository and add the configuration files to the *conf* folder. After that run:
    ```
    activator run
    ```

This will run using the *conf/application.conf* configuration

## Deployment operations

The folder *ops* contains scripts related to running Wazza locally (using a generated executable instead of *activator*)  and all the deployment tasks. All scripts must be executed on the *ops* directory.

To start the wazza executable simply run:
    ```
    python run.py ${WAZZA_FULL_PATH}/conf/application.conf
    ```

Inside the *ops* directory there is a folder called *deployment*. This folder contains scripts that generate the packages to deploy to EC2 and Elastic Beanstalk. To run the deployment tasks simply do:
    
    ```sh
    cd deployment
    python deploy.sh ${WAZZA_VERSION} # example: python deploy.py alpha
    ```

