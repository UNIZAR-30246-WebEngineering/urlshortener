# Getting Started with the URL Shortener project
[![Build Status](../../actions/workflows/ci.yml/badge.svg)](../../actions/workflows/ci.yml)

* [Overall structure](#overall-structure)
* [Run](#run)
* [Build and Run](#build-and-run)
* [Functionalities](#functionalities)
* [Delivery](#delivery)
* [Repositories](#repositories)
* [Reference Documentation](#reference-documentation)
* [Guides](#guides)

## Overall structure

The structure of this project is heavily influenced by
[the clean architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html):

* A `core` module where we define the domain entities and the functionalities
  (also known as uses cases, business rules, etc.). They do not know that this application.
  has a web interface or that data is stored in relational databases.
* A `repositories` module that knows how to store domain entities in a relational database.
* A `delivery` module that knows how to expose in the Web the functionalities.
* An `app` module that contains the main, the configuration (i.e. it links `core`, `delivery` and `repositories`),
  and the static assets (i.e. html files, JavaScript files, etc. )

Usually, if you plan to add a new feature, usually:

* You will add a new use case to the `core` module.
* If required, you will modify the persistence model in the `repositories` module.
* You will implement a web-oriented solution to expose to clients in the `delivery` module.

Sometimes, your feature will not be as simple, and it would require:

* To connect a third party (e.g. an external server).
  In this case you will add a new module named `gateway` responsible for such task.
* An additional application.  
  In this case you can create a new application module (e.g. `app2`) with the appropriate configuration to run this second server.

Features that require the connection to a third party or having more than a single app will be rewarded.

## Run

The application can be run as follows:

```shell
./gradlew :app:bootRun
```

Now you have a shortener service running at port 8080. You can test that it works as follows:

```shell
$ curl -v -d "url=http://www.unizar.es/" http://localhost:8080/api/link
*   Trying ::1:8080...
* Connected to localhost (::1) port 8080 (#0)
> POST /api/link HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.71.1
> Accept: */*
> Content-Length: 25
> Content-Type: application/x-www-form-urlencoded
> 
* upload completely sent off: 25 out of 25 bytes
* Mark bundle as not supporting multiuse
< HTTP/1.1 201 
< Location: http://localhost:8080/tiny-6bb9db44
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Tue, 28 Sep 2021 17:06:01 GMT
< 
* Connection #0 to host localhost left intact
{"url":"http://localhost:8080/tiny-6bb9db44","properties":{"safe":true}}%   
```

And now, we can navigate to the shortened URL.

```shell
$ curl -v http://localhost:8080/tiny-6bb9db44
*   Trying ::1:8080...
* Connected to localhost (::1) port 8080 (#0)
> GET /tiny-6bb9db44 HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.71.1
> Accept: */*
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 307 
< Location: http://www.unizar.es/
< Content-Length: 0
< Date: Tue, 28 Sep 2021 17:07:34 GMT
< 
* Connection #0 to host localhost left intact
```

## Build and Run

The uberjar can be built and then run with:

```shell
./gradlew build
java -jar app/build/libs/app.jar
```

## Functionalities

The project offers a minimum set of functionalities:

* **Create a short URL**.
  See in `core` the use case `CreateShortUrlUseCase` and in `delivery` the REST controller `UrlShortenerController`.

* **Redirect to a URL**.
  See in `core` the use case `RedirectUseCase` and in `delivery` the REST controller `UrlShortenerController`.

* **Log redirects**.
  See in `core` the use case `LogClickUseCase` and in `delivery` the REST controller `UrlShortenerController`.

The objects in the domain are:

* `ShortUrl`: the minimum information about a short url
* `Redirection`:  the remote URI and the redirection mode
* `ShortUrlProperties`: a handy way to extend data about a short url
* `Click`: the minimum data captured when a redirection is logged
* `ClickProperties`: a handy way to extend data about a click

## Delivery

The above functionality is available through a simple [API](http://localhost:8080/swagger-ui/index.html):

* `POST /api/link` which creates a short URL from data send by a form.
* `GET /tiny-{id}` where `id` identifies the short url, deals with redirects, and logs use (i.e. clicks).

In addition, `GET /` returns the landing page of the system.

## Repositories

All the data is stored in a relational database.
There are only two tables.

* **shorturl** that represents short url and encodes in each row `ShortUrl` related data,
* **click** that represents clicks and encodes in each row `Click` related data.

## Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.3/reference/htmlsingle/#boot-features-developing-web-applications)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/2.7.3/reference/htmlsingle/#boot-features-jpa-and-spring-data)

## Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

## Detekt

Execute detect without type resolution:

```bash
./gradlew detekt
```

Auto generate baseline file:

```bash
./gradlew detektBaseline
```

To view more deteckt commands check:

```bash
./gradlew tasks
```

## Sonarkube

If not installed please install in your computer docker and docker-compose.

Login into your docker hub account:

```bash
docker login
```

Go to the root of the project and execute:

```bash
cd sonarqube/
docker-compose up -d
```

To stop the sonarqube server:

```bash
docker-compose down
```

To check the sonarqube server go to:

```bash
http://localhost:9000
```

And then login with the default credentials:

```bash
login: admin
password: admin
```

To generate a token go to
    
```bash
http://localhost:9000/account/security
```

And then generate a token with the SONAR_TOKEN name for example.

To run sonarqube analysis:

```bash
./gradlew sonarqube -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<yourToken>
```

Nacho's credentials:

```bash
token: b6ec4fede6f51d64fcbc4e530587478c32b2c4bc
login: admin
password: pikachu
```
