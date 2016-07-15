This is a docker image for the Zanata server. This image runs a wildfly server with Zanata running on top of it. It does not run a database server for the Zanata application

```
This image has been tested with Docker 1.9
```

## Building

To build this docker image as "latest", simply type the following command:

```sh
$ docker build -t zanata/server .
```

If you want to build with a different tag, eg 3.8.4, use a command like this:

```sh
$ docker build -t zanata/server:3.8.4 .
```

Be careful about overwriting existing versions.

The `-t` parameter indicates the name and/or tag for the image.

## Running a Zanata server with Docker

### Run a database server container

Simply run

```sh
$ ./rundb.sh
```

This script will start a docker container with the database. You can inspect the script file to learn the exact docker command it's running.

The container will map the mysql data directory to `$HOME/docker-volumes/zanata-mariadb`. This can be changed from the script file.

The database can be accessed via tcp via the `mysql` command or by using any database administration tool. You need to get the actual mapped port on the host by typing `docker ps`. After you have the port, you can connect locally to the database. The following is an example to accomplish this using a locally installed mysql client:

```sh
mysql --protocol=tcp -h localhost --port=<PORT> -uzanata -pzanatapw zanata
```

Alternatively, a docker container can be used to connect to the database using the mysql client like this:

```sh
$ docker exec -it zanatadb mysql -uzanata -pzanatapw zanata
```

The username and password above are the default given by the `rundb.sh` script. If you wish to change them, edit the script file with your preferred credentials.

_Note: The server container will be called `zanatadb`. Once stopped, docker will prevent the `rundb.sh` script from running again until the stopped container is removed. To do this, just type:_

```sh
$ docker rm zanatadb
```

If you don't remove the container, and later want to simply restart this same instance, you can simply run

```sh
$ docker start zanatadb
```

## Run the Zanata development server container

To start the Zanata server run:

```sh
$ ./rundev.sh
```

This script will start another docker container with the Zanata server, and will log the server output. Unlike the database container, the server container will run in daemon mode.

The server will connect to the db server which was started in the previous step. It will also take the zanata.war from any war file in the `zanata-war/target` directory. This means a war file must be built beforehand (See `etc/scripts/quickbuild.sh`). You must make sure there is only one `zanata-*.war` file in this directory, otherwise this step will fail.

This container will have a mapped volume to your `$HOME/zanata` directory to store files, stats, caches, etc. This will allow for backups if you wish to switch between different versions of zanata for instance.

## Create an admin user

_This step is only required for empty databases. Since the `rundb.sh` script creates a mapped volume with all the mariadb data, it's not needed for subsequent runs._

At this point, you have a running Zanata instance... with no users created (!)

To create an admin user, you can connect to the database server and run the script at `/conf/admin-user-setup.sql` like this:

```sh
$ docker exec -i zanatadb  mysql -uzanata -pzanatapw zanata < conf/admin-user-setup.sql
```

_Note the -u and -p parameters to the mysql command must be followed by the database username and password as indicated in the rundb.sh file. The values above are the default values in the script._

This will create a username `admin` with password `admin1234`

## Access Zanata

The docker host will forward port 8080 to the container's 8080 port where Wildfly is listening.

Just open a browser and head to `http://IP_ADDRESS:8080`, where `IP_ADDRESS` is the docker host's address (again, this might localhost or some other IP address if running on OSX for example).

## DockerHub

The DockerHub site for Zanata images is here:
https://hub.docker.com/r/zanata/server/
