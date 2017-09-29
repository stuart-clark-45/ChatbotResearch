package com.smc.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Set;

public class Tweet {

  @Getter
  private ObjectId id;

  @Getter
  @Setter
  private String text;

  @Getter
  @Setter
  private Date created;

  @Getter
  @Setter
  private long tweetId;

  @Getter
  @Setter
  private String username;

  @Getter
  @Setter
  private long userId;

  @Getter
  @Setter
  private Set<String> hashtags;

}
