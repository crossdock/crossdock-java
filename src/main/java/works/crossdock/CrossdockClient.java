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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import works.crossdock.client.Behavior;

/**
 * Runs crossdock behaviors. Behaviors are passed as input to the client. Callers explicitly control
 * the start / stop of the client.
 */
public class CrossdockClient {
  private final int listenPort;
  private final Map<String, Behavior> behaviors;
  private ChannelFuture cancel;

  /**
   * Creates a client handle for crossdock tests.
   *
   * @param listenPort port to run the crossdock server on
   * @param inputBehaviors map of behaviors and actions to execute
   */
  public CrossdockClient(int listenPort, Map<String, Behavior> inputBehaviors) {
    this.listenPort = listenPort;
    this.behaviors = Collections.unmodifiableMap(new HashMap<>(inputBehaviors));
    this.cancel = null;
  }

  /**
   * Starts the client by starting underlying http server.
   *
   * @throws Exception if server fails to start
   */
  public void start() throws Exception {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(
            new ChannelInitializer<SocketChannel>() {
              @Override
              public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                    .addLast(new HttpServerCodec())
                    .addLast(new HttpServerKeepAliveHandler())
                    .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                    .addLast(new CrossdockServerInboundHandler(behaviors));
              }
            });

    cancel = bootstrap.bind("0.0.0.0", listenPort).sync();
    cancel
        .channel()
        .closeFuture()
        .addListener(
            new GenericFutureListener<Future<? super Void>>() {
              @Override
              public void operationComplete(Future<? super Void> future) throws Exception {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
              }
            });
  }

  /** Stops the channel that client is listening on. */
  public void stop() {
    if (cancel != null) {
      cancel.cancel(true);
    }
  }
}
