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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.apache.http.client.fluent.Request;
import org.junit.Test;
import works.crossdock.client.Behavior;

public class CrossdockIntegrationTest {

  @Test
  public void testSuccessEndtoEnd() throws Exception {
    class TestBehavior implements Behavior {
      @Override
      public CompletionStage<CrossdockResponse> run(CrossdockRequest request) {
        return CompletableFuture.completedFuture(
            new CrossdockResponse().success("Success from integration"));
      }
    }

    Map<String, Behavior> behaviorMap = new HashMap<>();
    behaviorMap.put("test", new TestBehavior());
    CrossdockClient crossdockClient = new CrossdockClient(8080, behaviorMap);
    crossdockClient.start();

    String successResponse =
        Request.Get("http://127.0.0.1:8080/?behavior=test")
            .connectTimeout(1000)
            .socketTimeout(1000)
            .execute()
            .returnContent()
            .asString();

    System.out.println(successResponse);
    List<Map<String, String>> resultList =
        new ObjectMapper()
            .readValue(successResponse, new TypeReference<List<Map<String, String>>>() {});
    assertEquals(resultList.size(), 1);
    Map<String, String> gotParamsMap = resultList.get(0);

    Map<String, String> wantedParamsMap = new HashMap<>();
    wantedParamsMap.put("output", "Success from integration");
    wantedParamsMap.put("status", "PASSED");
    for (Entry<String, String> gotEntry : gotParamsMap.entrySet()) {
      assertEquals("Success behavior", wantedParamsMap.get(gotEntry.getKey()), gotEntry.getValue());
    }

    crossdockClient.stop();
  }

  @Test
  public void testSkipEndtoEnd() throws Exception {
    Map<String, Behavior> behaviorMap = new HashMap<>();
    CrossdockClient crossdockClient = new CrossdockClient(8081, behaviorMap);
    crossdockClient.start();

    String skipResponse =
        Request.Get("http://127.0.0.1:8081/?behavior=test1")
            .connectTimeout(1000)
            .socketTimeout(1000)
            .execute()
            .returnContent()
            .asString();

    System.out.println(skipResponse);
    List<Map<String, String>> resultList =
        new ObjectMapper()
            .readValue(skipResponse, new TypeReference<List<Map<String, String>>>() {});
    assertEquals(resultList.size(), 1);
    Map<String, String> gotParamsMap = resultList.get(0);

    Map<String, String> wantedParamsMap = new HashMap<>();
    wantedParamsMap.put("output", "Unsupported behavior: test1");
    wantedParamsMap.put("status", "SKIPPED");
    for (Entry<String, String> gotEntry : gotParamsMap.entrySet()) {
      assertEquals("Skip behavior", wantedParamsMap.get(gotEntry.getKey()), gotEntry.getValue());
    }

    crossdockClient.stop();
  }

  @Test
  public void testErrorEndtoEnd() throws Exception {
    class TestBehavior implements Behavior {
      @Override
      public CompletionStage<CrossdockResponse> run(CrossdockRequest request) {
        return CompletableFuture.completedFuture(
            new CrossdockResponse().error("Error from integration"));
      }
    }

    Map<String, Behavior> behaviorMap = new HashMap<>();
    behaviorMap.put("test", new TestBehavior());
    CrossdockClient crossdockClient = new CrossdockClient(8082, behaviorMap);
    crossdockClient.start();

    String errorResponse =
        Request.Get("http://127.0.0.1:8082/?behavior=test")
            .connectTimeout(1000)
            .socketTimeout(1000)
            .execute()
            .returnContent()
            .asString();

    System.out.println(errorResponse);
    List<Map<String, String>> resultList =
        new ObjectMapper()
            .readValue(errorResponse, new TypeReference<List<Map<String, String>>>() {});
    assertEquals(resultList.size(), 1);
    Map<String, String> gotParamsMap = resultList.get(0);

    Map<String, String> wantedParamsMap = new HashMap<>();
    wantedParamsMap.put("output", "Error from integration");
    wantedParamsMap.put("status", "FAILED");
    for (Entry<String, String> gotEntry : gotParamsMap.entrySet()) {
      assertEquals("Error behavior", wantedParamsMap.get(gotEntry.getKey()), gotEntry.getValue());
    }

    crossdockClient.stop();
  }
}
