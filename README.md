Build status for master branch [![Build Status](https://travis-ci.org/dvla/vrm-assign-online.svg?branch=master)](https://travis-ci.org/dvla/vrm-assign-online)

DVLA Vehicles Online
====================

`vrm-assign-online` is the Web frontend for assigning a personalised registration number online.

Architectural overview
----------------------

### Presentation layer

This project encapsulates the presentation layer of the application.

The codebase is predominantly [Scala][scala] and is implemented against [Play][play-framework]: a 'full stack' Web
framework for the JVM.

### Microservices

Most complex business decisions are deferred to a network of [RESTful][rest] microservices. These are maintained through
separate projects:

-   `os-address-lookup`
-   `vehicle-and-keeper-lookup`

These services are mocked for automated testing, but must be running locally for manual testing/development of dependant
components within the presentation layer.

Development environment
-----------------------

1.  Each project must be checked-out to the same directory using the following names:

    -   `vrm-assign-online`
    -   `vehicle-and-keeper-lookup`
    -   `<the appropriate secrets repo>'
    -   `os-address-lookup`

2.  JDK 1.7 must be installed

3.  Install SASS. The [current documentation][install-sass] suggests:

        sudo gem install sass

4.  Install SBT.  The [current documentation][install-sbt] suggests:

        brew install sbt

5.  Install phantom-js for testing web pages that contain javascript:

        brew install phantomjs

6.  Increase 'permanent generation space' requirements for SBT.

    Create the file `~/.sbtconfig` with the following content:

        SBT_OPTS="$SBT_OPTS -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:PermSize=256M -XX:MaxPermSize=2048M"

7.  Decrypt secret keys:

        cd <<the appropriate secrets repo>>
        ./setup XYZ

    *where `XYZ` is an offline secret key obtained through a trusted team member*

8.  Install Chrome Driver:
    - Mac : `brew install chrome driver`
    - Windows :
        First Install Chocolatey Package manager:
        `@powershell -NoProfile -ExecutionPolicy unrestricted -Command “iex ((new-object net.webclient).DownloadString(‘http://chocolatey.org/install.ps1&#8242;))” && SET PATH=%PATH%;%systemdrive%\chocolatey\bin`
        Run Choclatey command:
        `choco install googlechrome`

Running the application
-----------------------

1.  Run the `vrm-assign-online` application:

        cd vrm-assign-online
        sbt run

2.  Open in Web browser:

        http://localhost:9000/

3.  Repeat *step 1* for each required microservice (if any).

### Running with production logging

To emulate production-level logging:

1.  Ensure `syslog` is configured. Details have been provided for [configuring `syslog` on OSX][syslog-osx].

2.  Run the `vrm-assign-online` application:

        cd vrm-assign-online
        ./startWithLog.sh
        
3.  Open in Web browser:

        http://localhost:9000/


### Testing the application

- Unit and Selenium tests written using Scalatest can be run using `sbt test`

- Acceptance tests written using Cucumber can be run using `sbt acceptance-tests/test-only`

- Execute Acceptance Tests on different environments/Urls
  `sbt`
  `eval System.setProperty("test.url","http://localhost:9000/")`
  `acceptance-tests/test-only`

- Execute Tests on different browsers, Tests can be run on `chrome` & `firefox`
  `sbt`
  `eval System.setProperty("browser.type","chrome")`
  `acceptance-tests/test-only`

- Acceptance tests Reporting


Session encryption
------------------

Please refer to the [session encryption][session-encryption] document for details on the encryption algorithm used.

[install-sass]: http://sass-lang.com/install "Install SASS"
[install-sbt]: http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt "Install SBT"
[rest]: https://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm "REST"
[play-framework]: http://www.playframework.com/ "Play Framework"
[scala]: http://www.scala-lang.org/ "Scala Language"
[syslog-osx]: syslog-osx.md "Configuring syslog on OSX"
[session-encryption]: encrypted-session-state.md "Session Encryption"
