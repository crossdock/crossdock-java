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
import java.util.Map;
import works.crossdock.client.Behavior;

public class CrossdockServerInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private final Map<String, Behavior> behaviors;

  /** Populates all the different behaviors supported. */
  public CrossdockServerInboundHandler(Map<String, Behavior> inputBehaviors) {
    behaviors = inputBehaviors;
  }

  private void healthCheck(ChannelHandlerContext ctx) {
    FullHttpResponse httpResponse =
        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
    return;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest)
      throws Exception {
    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(fullHttpRequest.uri());
    CrossdockRequest request = CrossdockRequest.fromQueryParameters(queryStringDecoder);
    String behavior = request.getParam("behavior");
    if (behavior == null) {
      healthCheck(ctx);
      return;
    }
    Behavior br = behaviors.get(behavior);
    CrossdockServerUtil.runBehavior(br, request)
        .whenComplete(
            (response, ex) -> {
              if (ex != null) {
                ctx.fireExceptionCaught(ex);
              } else {
                writeResponseAndCloseChannel(ctx, response);
              }
            });
  }

  private void writeResponseAndCloseChannel(ChannelHandlerContext ctx, CrossdockResponse response) {
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
    httpResponse.headers().add("connection", "close");
    ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
  }
}
