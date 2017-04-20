/*
 * Copyright (c) 2017, Uber Technologies, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package works.crossdock;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import org.apache.http.client.fluent.Request;
import org.junit.Test;
import works.crossdock.client.Behavior;

public class CrossdockIntegrationTest {
  private static final int crossdockPort = 8080;

  @Test
  public void testEndtoEnd() throws Exception {
    class TestBehavior implements Behavior {

      @Override
      public CompletionStage<CrossdockResponse> run(CrossdockRequest request) throws Exception {
        return CompletableFuture.completedFuture(
            new CrossdockResponse().success("Success from integration"));
      }
    }

    Map<String, Behavior> behaviorMap = new HashMap<>();
    behaviorMap.put("test", new TestBehavior());
    CrossdockClient crossdockClient = new CrossdockClient(crossdockPort, behaviorMap);
    Future future = crossdockClient.start();

    String successResponse =
        Request.Get("http://127.0.0.1:8080/?behavior=test")
            .connectTimeout(1000)
            .socketTimeout(1000)
            .execute()
            .returnContent()
            .asString();
    assertEquals(
        "Integration test",
        "[{\"output\":\"Success from integration\",\"status\":\"passed\"}]",
        successResponse);

    String errorResponse =
        Request.Get("http://127.0.0.1:8080/?behavior=test1")
            .connectTimeout(1000)
            .socketTimeout(1000)
            .execute()
            .returnContent()
            .asString();
    assertEquals(
        "Failed behavior",
        "[{\"output\":\"Unsupported behavior: test1\",\"status\":\"skipped\"}]",
        errorResponse);

    future.cancel(true);
  }
}
