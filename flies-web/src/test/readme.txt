If you want to run tests using the Eclipse TestNG plugin, you'll need to add
these jars to the top of your TestNG classpath.  Using the Run Dialog, select
xml suite to run, and add /lib/test/jboss-embedded-all.jar, 
/lib/test/hibernate-all.jar, /lib/test/thirdparty-all.jar, /lib/jboss-embedded-api.jar, 
/lib/jboss-deployers-client-spi.jar, /lib/jboss-deployers-core-spi.jar,  and 
/bootstrap as the first entries in the User classpath.

To add tests to your project create a TestNG xml descriptor called *Test.xml e.g.
FooTest.xml next to your test classes and run ant test.