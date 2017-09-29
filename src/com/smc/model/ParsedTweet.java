package com.smc.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

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

  @Getter
  @Setter
  private List<Token> tokens;

  public ParsedTweet() {
    this.keywords = new HashSet<>();
    this.hashtags = new HashSet<>();
  }

  public void addHashTag(String hashtag) {
    hashtags.add(hashtag);
  }

}
