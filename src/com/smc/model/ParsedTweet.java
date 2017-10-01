package com.smc.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.smc.util.Parser;

import lombok.Getter;
import lombok.Setter;

/**
 * Module used to represent tweets in a way that enables analysis to be performed.
 *
 * @author Stuart Clark
 */
public class ParsedTweet {

  @Getter
  @Setter
  private String unparsed;


  /**
   * The set of hashtags that were used in the tweet.
   */
  @Getter
  @Setter
  private Set<String> hashtags;

  /**
   * The key words for the tweet
   */
  @Getter
  @Setter
  private Set<String> keywords;

  /**
   * The key phrases for the tweet. These consist of both keywords and hashtags.
   */
  @Getter
  @Setter
  private Set<String> keyphrases;

  /**
   * The results of {@link Parser#getTokens()} for the tweet.
   */
  @Getter
  @Setter
  private List<Token> tokens;

  @Getter
  @Setter
  private boolean retweet;

  public ParsedTweet() {
    this.keywords = new HashSet<>();
    this.hashtags = new HashSet<>();
  }

}
