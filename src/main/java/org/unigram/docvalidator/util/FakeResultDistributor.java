/**
 * DocumentValidator
 * Copyright (c) 2013-, Takahiko Ito, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package org.unigram.docvalidator.util;
/**
 * FakeResultDistributor does nothing. this class is just for testing.
 */
public class FakeResultDistributor implements ResultDistributor {
  public int flushResult(ValidationError err) {
    return 0;
  }

  public FakeResultDistributor() {
    super();
  }

  @Override
  public void setFormatter(Formatter formatter) {}

  @Override
  public void flushHeader() {}

  @Override
  public void flushFooter() {}
}
