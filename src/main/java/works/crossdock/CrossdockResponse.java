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

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/** Represents the response from Crossdock test run. */
public class CrossdockResponse {
  @Getter private final List<TestResult> results = new ArrayList<>();

  /**
   * Sets the success for a crossdock test.
   *
   * @param message description about the test
   * @return CrossdockResponse with succeeded test
   */
  public CrossdockResponse success(String message) {
    results.add(new TestResult(message, Status.PASSED.getName()));
    return this;
  }

  /**
   * Sets the error for a crossdock test.
   *
   * @param message description about the test
   * @return CrossdockResponse with errored test set
   */
  public CrossdockResponse error(String message) {
    results.add(new TestResult(message, Status.FAILED.getName()));
    return this;
  }

  /**
   * Sets the skipped response for a crossdock test.
   *
   * @param message description about the test
   * @return CrossdockResponse with skipped test set
   */
  public CrossdockResponse skipped(String message) {
    results.add(new TestResult(message, Status.SKIPPED.getName()));
    return this;
  }
}
