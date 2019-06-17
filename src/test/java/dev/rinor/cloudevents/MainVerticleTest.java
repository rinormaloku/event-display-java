package dev.rinor.cloudevents;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventBuilder;
import io.cloudevents.http.reactivex.vertx.VertxCloudEvents;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpClientRequest;


import java.net.URI;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

  private Vertx vertx;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(MainVerticle.class.getName(), tc.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void testThatTheServerIsStarted(TestContext tc) {
    Async async = tc.async();
    vertx.createHttpClient().getNow(8080, "localhost", "/healthZ", response -> {
      tc.assertEquals(response.statusCode(), 200);
      response.bodyHandler(body -> {
        tc.assertTrue(body.length() > 0);
        async.complete();
      });
    });
  }

  @Test
  public void testCloudEventReceived(TestContext tc) {
    Async async = tc.async();
    final CloudEvent<String> cloudEvent = new CloudEventBuilder<String>()
      .specVersion("0.2")
      .source(URI.create("http://knative-eventing.com"))
      .id("foo-bar")
      .type("pushevent")
      .data("{\"foo\":\"bar\"}}")
      .build();

    final HttpClientRequest req = vertx.createHttpClient().post(8080, "localhost", "/");
    req.handler(resp -> tc.verify(h -> {
      assertThat(resp.statusCode()).isEqualTo(200);
      async.complete();
    }));

    VertxCloudEvents.create().writeToHttpClientRequest(cloudEvent, req);
    req.end();
  }
}
