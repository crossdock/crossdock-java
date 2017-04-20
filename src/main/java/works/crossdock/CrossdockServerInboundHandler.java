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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import works.crossdock.client.Behavior;

public class CrossdockServerInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private final Map<String, Behavior> behaviors = new HashMap<String, Behavior>();

  /** Populates all the different behaviors supported. */
  public CrossdockServerInboundHandler(Map<String, Behavior> inputBehaviors) {
    for (Entry<String, Behavior> entry : inputBehaviors.entrySet()) {
      behaviors.put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest)
      throws Exception {
    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(fullHttpRequest.uri());
    CrossdockRequest request = populateRequest(queryStringDecoder);
    String behavior = request.getParam("behavior");
    if (behavior == null) {
      FullHttpResponse httpResponse =
          new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
      ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
      return;
    }
    Behavior br = behaviors.get(behavior);
    runBehavior(br, request)
        .whenComplete(
            (response, ex) -> {
              if (ex != null) {
                ctx.fireExceptionCaught(ex);
              } else {
                writeAndCloseChannel(ctx, response);
              }
            });
  }

  /**
   * Runs the request against the passed behavior.
   *
   * @param br behavior to run against
   * @param req request to run
   * @return CompletionStage that contains crossdockResponse
   * @throws Exception if behavior throws an exception
   */
  public CompletionStage<CrossdockResponse> runBehavior(Behavior br, CrossdockRequest req)
      throws Exception {
    if (br == null) {
      return CompletableFuture.completedFuture(
          new CrossdockResponse().skipped("Unsupported behavior: " + req.getParam("behavior")));
    }

    return br.run(req)
        .exceptionally(
            ex -> {
              String exceptionAsString = Throwables.getStackTraceAsString(ex);
              return new CrossdockResponse().error(exceptionAsString);
            });
  }

  private void writeAndCloseChannel(ChannelHandlerContext ctx, CrossdockResponse response) {
    ObjectMapper objectMapper = new ObjectMapper();
    byte[] body;
    HttpResponseStatus status = HttpResponseStatus.OK;
    try {
      body = objectMapper.writeValueAsBytes(response.getResults());
    } catch (JsonProcessingException e) {
      body = e.getMessage().getBytes(Charsets.UTF_8);
      status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }
    FullHttpResponse httpResponse =
        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(body));
    ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
  }

  /**
   * Constructs a crossdockRequest from passed queryStringDecoder.
   *
   * @param queryStringDecoder decoder to construct request from
   * @return crossDockRequest formed from the queryStringDecoder
   */
  public CrossdockRequest populateRequest(QueryStringDecoder queryStringDecoder) {
    Map<String, List<String>> queryParams = queryStringDecoder.parameters();
    Map<String, String> params = new HashMap<>();
    queryParams.forEach(
        (key, value) -> {
          if (value != null && value.size() > 0) {
            params.put(key, value.get(0));
          }
        });
    return new CrossdockRequest(params);
  }
}
