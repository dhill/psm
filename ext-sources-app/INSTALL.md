       Installation instructions for the Provider Screening Module
       ===========================================================

***NOTE: 2017-05-25: These instructions are still very incomplete and
   are a work in progress.  We welcome suggestions on improving
   them.***

These are instructions for building and deploying the external
sources app, which is part of the
[Provider Screening Module](https://github.com/OpenTechStrategies/psm).

# Background and Current Deployment Status

2017-05-25: The external sources app is not yet ready for production
or development deployment.

The PSM was originally developed to run in the open source web
application server Apache JBoss (now called WildFly).  Somewhat late
in the PSM's development, it was retargeted to the IBM WebSphere
Application Server (WAS) 8.5, in order to better support a particular
state's MMIS environment.  Our plan is to retarget the PSM to WildFly
(formerly JBoss), though still keeping all of the functionality
additions made while the PSM was in its WebSphere interregnum.

This INSTALL.md file will be continuously improved as we work.  When
it loses the warning at the top, that will mean we expect the PSM to
be deployable in WildFly.  We are currently evaluating the additional
resources it would take to continue development support for WebSphere
deployment.

Note that the repository currently depends on access to a Oracle
database.  We intend to shift towards PostgreSQL in order to eliminate
a proprietary dependency.

You can use Docker to run the current development version of the PSM
(though note that it doesn't yet include external sources).  That
would obviate all the manual configuration steps listed in this file.
See ../docker/README.md for current progress.

# Overview

The external sources app is a Java EE Enterprise Application. It depends
on a correctly-configured Java EE Application Server. While it was originally
written for the Java EE 6 profile, it is currently being ported to run on Java
EE 7 Application Servers, starting with WildFly 10.

## System Requirements

These requirements are based on our understanding of the application at this
time, and will evolve as we understand it more.

### Hardware

- **Memory**: 8 GB should be enough for a test system
- **CPU**: TBA; provisioning CPU proportional to memory (whatever that looks
  like in your environment) should be reasonable.
- **Storage**: 10 GB of storage for WildFly, the PSM repository, and its
  dependencies should be plenty.

### Software

- **Operating System**: we recommend the stable Debian 8 (jessie). If
  that's not feasible for your environment, any of the supported
  WildFly 10.1 operating systems should work, but our ability to help
  troubleshoot issues that come up may be limited.  Once we test this
  on a few more platforms, we will expand the list of compatible
  operating systems to include onther Linux distributions.
- **Java**: We're using OpenJDK 8, which is currently 8u121, but you should
  keep up with the latest releases and post if you have issues relating to
  upgrading.
- **Java EE Application Server**: currently WildFly 10.1. We may support other
  application servers in the future.

### Database

We're testing with latest stable PostgreSQL, currently 9.6.2. PostgreSQL 10
will be released shortly and we hope/intend to verify compatibility with that.

- **Storage**: TBA. We haven't started integrating with any external data
  sources yet, which will likely be the largest driver of storage requirements.
  We suggest starting with 10 GB for the database, and have a plan to expand or
  reprovision later.

# Prerequisites

1. A [Java 8](https://www.java.com) JRE and JDK. We are testing with OpenJDK 8,
   which you can install on Debian-like systems with
   `sudo apt install openjdk-8-jdk-headless`.
1. [Ant](https://ant.apache.org/) for building: `sudo apt install ant`.

1. [PostgreSQL 9.6](https://www.postgresql.org/). We are testing with
   PostgreSQL 9.6.2.

# Configuring WildFly

Building and deploying the external sources application requies WildFly to be installed and
configured. See also the [WildFly 10 Getting Started
Guide](https://docs.jboss.org/author/display/WFLY10/Getting+Started+Guide).

1. Get Wildfly: Visit
   [http://wildfly.org/downloads/](http://wildfly.org/downloads/). Download
   the [10.1.0.Final full
   distribution](http://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.tar.gz).

   ```ShellSession
   $ cd /path/to/this_psm_repo
   $ # this should be a peer directory, so:
   $ cd ..
   $ tar -xzf wildfly-10.1.0.Final.tar.gz
   $ cd wildfly-10.1.0.Final
   ```

1. Add a WildFly management console user

   ```ShellSession
   $ ./bin/add-user.sh <wildfly-username> <wildfly-password>

1. Stop the server if it is already running:

   ```ShellSession
   $ ./bin/jboss-cli.sh --connect --command=:shutdown
   ```

1. The `standalone-full` profile includes messaging, which PSM
   requires.  External sources does not require messaging (TODO: is
   this true), so your choice of whether to run full or not might
   depend on whether you are running the PSM and ext sources on the
   same server.  At any rate, `standalone-full.xml` lives in the
   WildFly directory, at
   `standalone/configuration/standalone-full.xml`. To start the
   server:

   ```ShellSession
   $ ./bin/standalone.sh -c standalone-full.xml
   ```

   WildFly, by default, is only accessible to localhost. If you are running
   WildFly on a remote system, you may start the server in a way that allows
   remote connections:

   ```ShellSession
   $ ./bin/standalone.sh \
       -c standalone-full.xml \
       -b 0.0.0.0 \
       -bmanagement 0.0.0.0
   ```

   Be careful of the security implications of exposing the management interface
   to the internet! These instructions are for a **development** environment,
   not for a production environment.

1. Check that the server is running by visiting
   [http://localhost:9990/](http://localhost:9990/) for the management
   console and [https://localhost:8443/](https://localhost:8443/) for
   the app(s) it hosts.  This should be either none or perhaps the
   PSM, if you've already set up the PSM app.

## Configure services

### Mail

If you are using a debugging mail server such as MailCatcher, update the
outgoing SMTP port and add a mail server without credentials:

```ShellSession
$ ./bin/jboss-cli.sh --connect << EOF
/socket-binding-group=standard-sockets/remote-destination-outbound-socket-binding=mail-smtp:write-attribute(name=port,value=1025)
/subsystem=mail/mail-session="java:/Mail":add(jndi-name="java:/Mail")
/subsystem=mail/mail-session="java:/Mail"/server=smtp:add(outbound-socket-binding-ref=mail-smtp)
EOF
```

If you are using a production mail server, add a mail session with a JNDI name
of `java:/Mail` to your application server with the appropriate credentials
using the command line or web interface.

### Database

Download the [PostgreSQL JDBC driver](https://jdbc.postgresql.org/) and deploy
it to your application server:

```ShellSession
$ ./bin/jboss-cli.sh --connect --command="deploy ../postgresql-{VERSION}.jar"
```

You will need a database user, and a database owned by that user:

```ShellSession
$ sudo -u postgres createuser --pwprompt extsources
$ sudo -u postgres createdb --owner=extsources extsources
```

We recommend that the external sources and psm databases be separated
for security reasons.  The extsources user should have no access to
the psm database.  The psm user should only have read access to the
extsources databse.


# Building
1. Fill in your local properties:

   ```ShellSession
   $ cd ../psm/ext-sources-app
   $ cp build.properties.template build.properties
   $ favorite-editor build.properties
   ```

1. Build the application with `ant`. This depends on libraries provided by the
   application server.

   ```ShellSession
   $ cd ../psm/ext-sources-app
   $ ant dist
   Buildfile: /path/to/psm/ext-sources-app/build.xml
   ...[cut]...
   dist:
         [ear] Building ear: /path/to/psm/ext-sources-app/build/cms-external-sources.ear
   ```

1. Deploy the built app: you can use the Wildfly Management Console UI at
   [http://localhost:9990/](http://localhost:9990/), log in with your username
   and password, and do the following: Deployments > Add > Upload a new
   deployment > browse to file." Alternatively, you can use the command line
   interface:

   ```ShellSession
   $ /path/to/wildfly/bin/jboss-cli.sh --connect \
       --command="deploy /path/to/psm/ext-sources-app/build/cms-external-sources.ear"
   ```

   If you have a previous build deployed already, you can replace the
   deployment in the UI or add the `--force` switch after `deploy`.
