package dev.rinor.cloudevents;


import io.cloudevents.http.reactivex.vertx.VertxCloudEvents;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {


  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start() {
    LOGGER.info("Verticle started");

    Router router = Router.router(vertx);

    router.get("/healthZ").handler(this::healthCheck);

    router.route("/").handler(req -> {
        VertxCloudEvents.create().rxReadFromRequest(req.request())
          .subscribe((receivedEvent, throwable) -> {
            if (receivedEvent != null) {
              LOGGER.fatal("The event type: " + receivedEvent.getType());
              LOGGER.fatal("->" + receivedEvent.toString());
            }
          });
        req.response().end();
      }
    );

    vertx.createHttpServer()
      .requestHandler(router)
      .rxListen(8080)
      .subscribe(server -> System.out.println("Server running!"));
  }

  private void healthCheck(RoutingContext routingContext) {
    routingContext.response().end("Healthy!");
  }
}
