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

The steps to get a Zanata development server running are:

 1. Ensure that docker is properly set up.
 2. Run a database server container.
 3. Run the Zanata development server container.
 4. Create an admin user.

When you have performed all these steps, you can sign in with the admin account
to configure the server and set up any other users and data you need.

If you build a new zanata war, kill the docker server (Ctrl+C) then re-run it
in the same way you first started it.


### Docker setup

The following instructions are to set up docker on a Fedora system after it has
been installed with `dnf install docker`. For docker setup on other operating
systems, see [Install Docker Engine - Docker](https://docs.docker.com/engine/installation/)

To set up docker you need to:

 - ensure docker service is running
 - create a docker group and add yourself to it (otherwise you will have to
   use `sudo` for all your docker commands)

To check if the docker service is running, run the following command and look
for `Active: active`:

```
systemctl status docker
```

If it is not active, start it with:

```
systemctl start docker
```


Once the service is active, it may still be disabled. You can enable it with:

```
systemctl enable docker.service
```


To create a docker group, follow these
[instructions to create a docker group on Fedora](https://docs.docker.com/engine/installation/linux/fedora/#/create-a-docker-group)


### Run a database server container

Simply run

```sh
$ ./rundb.sh
```

This script will start a docker container with the database. You can inspect the script file to learn the exact docker command it's running.

The container by default will map the mysql data directory to `$HOME/docker-volumes/zanata-mariadb`. This can be changed from the script file.

If you give the script ```-e``` option (stands for ephemeral), it will not use any volume mapping. This means any data you save in Zanata using this mode will be lost once the container is stopped. It will also remove itself once stopped (e.g. no need to call docker rm zanatadb). This is useful for testing a fresh copy of Zanata instance.

The database can be accessed via tcp via the `mysql` command or by using any database administration tool. You need to get the actual mapped port on the host by typing `docker ps`, e.g. based on the below output I would use port 32768:

```sh
$ docker ps
... PORTS                     NAMES
... 0.0.0.0:32768->3306/tcp      zanatadb
```

After you have the port, you can connect locally to the database. The following is an example to accomplish this using a locally installed mysql client:


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
$ ./rundev.sh [-p <port offset number>]
```

This script will start another docker container with the Zanata server, and will log the server output. Unlike the database container, the server container will run in daemon mode.

You can offset the standard ports if you want to avoid port conflicts on your host machine. e.g. you have other container/instance running and listening to port 8080. Give option `-p 100` will offset standard JBoss port by 100. e.g. server will be running on http://localhost:8180/ and debug port will be 8887.

The server will connect to the db server which was started in the previous step.

This container will have a mapped volume to your `$HOME/docker-volumes/zanata` directory to store files, stats, caches, etc. This will allow for backups if you wish to switch between different versions of zanata for instance.

This container will also map `$HOME/docker-volumes/zanata-deployments` directory to the container's JBoss deployments directory.

You will need to hard link or copy your war or exploded war to that folder to get it deployed. If you use hard link, once you rebuild the war and if the file name stays the same (e.g. same snapshot version), it will also get automatically detected.
E.g under linux:
```sh
$ln zanata-war/target/zanata-<versuin>.war ~/docker-volumes/zanata-deployments/ROOT.war
```


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
