# rest-easy-cdi
An example of JAX-RS (RestEasy) with CDI (Weld)

Run using:
`./gradlew clean build run`

##### CDI
- [What is beans xml](https://github.com/cdi-spec/cdi-spec.org/blob/master/_faq/intro/4-what-is-beans-xml-and-why-do-i-need-it.asciidoc)
- [Bean defining annotations (CDI 2.0)](http://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#bean_defining_annotations)

#### JAX-RS 
- [JAX-RS 2.1 Specs](https://download.oracle.com/otn-pub/jcp/jaxrs-2_1-final-eval-spec/jaxrs-2_1-final-spec.pdf?AuthParam=1542885786_3d58818373e05c37e4cfec30941eb367)

##### RestEasy
- [RestEasy 3.6.2.Final Docs](https://docs.jboss.org/resteasy/docs/3.6.2.Final/userguide/html/index.html)
- [Default scopes](https://docs.jboss.org/resteasy/docs/3.6.2.Final/userguide/html/CDI.html#d4e2782)
- [CDI Integration](https://developer.jboss.org/wiki/RESTEasy-CDIIntegration)

##### Weld
- [Weld 3.0.5.Final Docs](https://docs.jboss.org/weld/reference/3.0.5.Final/en-US/html/index.html)
- [Configuration](https://docs.jboss.org/weld/reference/3.0.5.Final/en-US/html/configure.html) 
- [Undertow](http://docs.jboss.org/weld/reference/3.0.5.Final/en-US/html/environments.html#_undertow)
- [Implicit Bean Archive Support](http://docs.jboss.org/weld/reference/3.0.5.Final/en-US/html/environments.html#_implicit_bean_archive_support)

##### StackOverflow
[Gradle bean discovery problems](https://stackoverflow.com/questions/30255760/bean-discovery-problems-when-using-weld-se-with-gradle-application-plugin#answer-30325614)

Side quests
- https://stackoverflow.com/questions/25385802/inject-context-cdi-servlet-into-new-futuretask-thread
- https://youtrack.jetbrains.com/issue/IDEA-175172
- https://docs.oracle.com/javaee/7/tutorial/cdi-adv-examples003.htm#JEETT01139
- https://docs.jboss.org/weld/reference/latest/en-US/html/ri-spi.html#_injection_services
- https://blogs.oracle.com/arungupta/totd-151:-transactional-interceptors-using-cdi-extensible-java-ee-6
- http://docs.jboss.org/weld/reference/latest/en-US/html/interceptors.html
- http://deltaspike.apache.org/documentation/jpa.html
- http://docs.jboss.org/cdi/spec/1.0/html/injectionelresolution.html#observers