package com.smc.model;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Used to store a single token and the results of annotations obtained using
 * 
 * @author Stuart Clark
 */
@EqualsAndHashCode
@ToString
public class Token implements Serializable {

  private static final long serialVersionUID = 1115320868857337412L;

  @Getter
  private String text;

  /**
   * The part of speech for the token. e.g. verb, noun etc.
   */
  @Getter
  @Setter
  private String pos;

  private Token() {
    // For morphia
  }

  public Token(String text) {
    this.text = text;
  }

}
