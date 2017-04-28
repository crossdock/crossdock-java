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

import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CrossdockRequest {
  @NonNull private final Map<String, String> params;

  /**
   * Returns the param for the passed key.
   *
   * @param key name of param to lookup
   * @return value of param if found, null otherwise
   */
  public String getParam(String key) {
    return params.get(key);
  }

  /**
   * Constructs a crossdockRequest from the passed queryStringDecoder.
   *
   * @param queryStringDecoder decoder to construct request from
   * @return crossDockRequest formed from the queryStringDecoder
   */
  public static CrossdockRequest fromQueryParameters(QueryStringDecoder queryStringDecoder) {
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
