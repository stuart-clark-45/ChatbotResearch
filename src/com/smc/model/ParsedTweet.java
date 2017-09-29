package com.smc.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public class ParsedTweet {

  @Getter
  @Setter
  private String unparsed;

  @Getter
  @Setter
  private Set<String> keywords;

  @Getter
  @Setter
  private Set<String> hashtags;

  public ParsedTweet() {
    this.keywords = new HashSet<>();
    this.hashtags = new HashSet<>();
  }

}
