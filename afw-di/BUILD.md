# Building

When building this library, there is a small problem: The javax.inject jar has never been
published as a Java 9 compliant module, but only as a Java 8 compliant jar file.

There is jakarta.inject:jakarta.inject-api:1.0.5, which would provide exactly that. However,
this one conflicts with jakarta.inject:jakarta.inject-api:2.0.1.MR, so we can't use it.

The trick we are using is: We use the 1.0.5 file anyways, but under different Maven coordinates.
The following steps are necessary:

1. Download the 1.0.5 file from [Maven Central](https://repo1.maven.org/maven2/jakarta/inject/jakarta.inject-api/1.0.5/jakarta.inject-api-1.0.5.jar) and store it temporarily into a suitable location. In what follows, I will assume
*c:\tmp*.
2. Deploy the downloaded jar file to your local Maven repository by executing the following command:

``` cmd
mvn install:install-file -Dfile=c:\tmp\jakarta.inject-api-1.0.5.jar
    -DgroupId=com.github.jochenw.javax.inject -DartifactId=javax.inject -Dversion=1.0.5 -Dpackaging=jar
    -DgeneratePom=true
```

The above must be entered on a single line, without any line breaks.




 