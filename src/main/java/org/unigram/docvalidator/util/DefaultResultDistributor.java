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

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * An implementation of ResultDistributor which flush the result into
 * given output stream.
 */
public class DefaultResultDistributor implements ResultDistributor {
  /**
   * constructor.
   * @param os output stream
   */
  public DefaultResultDistributor(OutputStream os) {
    super();
    if (os == null) {
      throw new IllegalArgumentException("argument OutputStream is null");
    }
    writer = new PrintWriter(os);
    formatter = new PlainFormatter();
  }

  /**
   * constructor.
   * @param ps output stream
   */
  public DefaultResultDistributor(PrintStream ps) {
    if (ps == null) {
      throw new IllegalArgumentException("argument PrintStream is null");
    }
    writer = new PrintWriter(ps);
  }

  /**
   * output given validation error.
   * @param err validation error
   */
  public int flushResult(ValidationError err) {
    if (err == null) {
      throw new IllegalArgumentException("argument ValidationError is null");
    }
    writer.println(formatter.convertError(err));
    writer.flush();
    return 0;
  }

  @Override
  public void flushHeader() {
    String header = formatter.header();
    if (header != null) {
      writer.println(header);
    }
  }

  @Override
  public void flushFooter() {
    String footer = formatter.footer();
    if (footer != null) {
      writer.println(footer);
      writer.flush();
    }
  }

  @Override
  public void setFormatter(Formatter formatter) {
    if (formatter == null) {
      throw new IllegalArgumentException("arugment formatter is null");
    }
    this.formatter = formatter;
  }

  private Formatter formatter;

  private PrintWriter writer;

}
