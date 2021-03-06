package com.smc.model;

import java.util.Date;
import java.util.Set;

import org.bson.types.ObjectId;

import lombok.Getter;
import lombok.Setter;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

/**
 * Basic model for imported tweets.
 */
public class Tweet {

  @Id
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
  @Indexed(unique = true)
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
