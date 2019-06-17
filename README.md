# Event Display with CloudEvents Java SDK (Vert.x)

This project is the Java implementation of the Event Display Service that dumps incoming messages to its log. The official Go implementation can be found at this [link](https://github.com/knative/eventing-contrib/blob/master/cmd/event_display/main.go) 

## Getting started
For local development you can simply execute:
```
mvn install
```

Otherwise to get a Docker Image just build from the Dockerfile.

### Testing it out in Knative

For quickly testing it out in Knative follow the guide under [Container Source](https://github.com/knative/docs/blob/master/docs/eventing/samples/container-source/README.md) and replace image for the service event-display to the following image:
`index.docker.io/rinormaloku/event_display`

Producing the following knative service definition:

```yaml
apiVersion: serving.knative.dev/v1alpha1
kind: Service
metadata:
  name: event-display
spec:
  runLatest:
    configuration:
      revisionTemplate:
        spec:
          container:
            image: index.docker.io/rinormaloku/event_display
```


## Important code snippets

```java
Router router = Router.router(vertx);

router.get("/healthZ").handler(this::healthCheck);

router.route("/").handler(req -> {
    VertxCloudEvents.create().rxReadFromRequest(req.request())
      .subscribe((receivedEvent, throwable) -> {
        if (receivedEvent != null) {
          System.out.printf("☁️  cloudevents.Event:\n%s\n", receivedEvent.toString());
        }
      });
    req.response().end();
  }
);

vertx.createHttpServer()
  .requestHandler(router)
  .rxListen(8080)
  .subscribe(server -> System.out.println("Server running!"));
```

## Important Dependencies

```xml
<dependency>
  <groupId>io.vertx</groupId>
  <artifactId>vertx-rx-java2</artifactId>
</dependency>
<dependency>
  <groupId>io.cloudevents</groupId>
  <artifactId>http-vertx</artifactId>
  <version>0.2.1</version>
</dependency>
```
