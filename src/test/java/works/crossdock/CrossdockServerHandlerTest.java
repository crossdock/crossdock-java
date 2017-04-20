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

import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.Test;
import works.crossdock.client.Behavior;

public class CrossdockServerHandlerTest {
  @Test
  public void testPopulateRequest() {
    System.out.println("Runnign tests");
    Map<String, String> paramsMap = new HashMap<>();
    paramsMap.put("key1", "value1");
    paramsMap.put("key2", "value2");
    CrossdockServerInboundHandler serverInboundHandler =
        new CrossdockServerInboundHandler(new HashMap<>());
    QueryStringDecoder decoder =
        new QueryStringDecoder("http://fakerequest?key1=value1&key2=value2");
    CrossdockRequest gotRequest = serverInboundHandler.populateRequest(decoder);
    for (Entry<String, String> params : paramsMap.entrySet()) {
      assertEquals(
          "Test CrossdockRequest", params.getValue(), gotRequest.getParam(params.getKey()));
    }
  }

  @Test
  public void testRunBehavior() throws Exception {
    class TestBehavior implements Behavior {
      @Override
      public CompletionStage<CrossdockResponse> run(CrossdockRequest request) throws Exception {
        CrossdockResponse crossdockResponse =
            new CrossdockResponse().success(request.getParam("ping"));
        return CompletableFuture.completedFuture(crossdockResponse);
      }
    }

    TestBehavior testBehavior = new TestBehavior();
    CrossdockServerInboundHandler serverInboundHandler =
        new CrossdockServerInboundHandler(new HashMap<>());
    QueryStringDecoder decoder = new QueryStringDecoder("http://fakerequest?ping=pong");
    CrossdockRequest request = serverInboundHandler.populateRequest(decoder);
    CrossdockResponse crossdockResponse =
        serverInboundHandler.runBehavior(testBehavior, request).toCompletableFuture().get();
    assertEquals("Test runBehavior", crossdockResponse.getResults().get(0).getOutput(), "pong");
  }
}
