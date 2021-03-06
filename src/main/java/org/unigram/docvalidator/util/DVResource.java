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
 * Contains Settings used thoughout DocumentValidator.
 */
public final class DVResource {
  /**
   * constructor.
   * @param validatorConf settings of Validators
   */
  public DVResource(ValidatorConfiguration validatorConf) {
    super();
    this.configuration = validatorConf;
    this.characterTable = new CharacterTable();
  }

  /**
   * constructor.
   * @param validatorConf settings of Validators.
   * @param characterConf settings of characters and symbols
   */
  public DVResource(ValidatorConfiguration validatorConf,
      CharacterTable characterConf) {
    super();
    this.configuration = validatorConf;
    this.characterTable = characterConf;
  }

  /**
   * get Configuration.
   * @return Configuration
   */
  public ValidatorConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * get CharacterTable.
   * @return CharacterTable
   */
  public CharacterTable getCharacterTable() {
    return characterTable;
  }

  private ValidatorConfiguration configuration;

  private CharacterTable characterTable;
}
