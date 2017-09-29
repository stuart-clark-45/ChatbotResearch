package com.smc.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * Module used to represent tweets in a way that enables analysis to be performed.
 */
public class ParsedTweet {

  @Getter
  @Setter
  private String unparsed;

  @Getter
  @Setter
  private Set<String> keywords;

  @Getter
  @Setter
  private Set<Phrase> keyphrases;

  @Getter
  @Setter
  private Set<String> hashtags;

  @Getter
  @Setter
  private List<Token> tokens;

  public ParsedTweet() {
    this.keywords = new HashSet<>();
    this.hashtags = new HashSet<>();
  }

}
