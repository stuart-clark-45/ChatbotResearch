package com.smc.model;

import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import lombok.Getter;
import lombok.Setter;

/**
 * Model used to represent the result of aggregations that count the number of occurrences of
 * {@code llong}.
 *
 * @author Stuart Clark
 */
public class LongCountResult {

  @Id
  @Getter
  private Long llong;

  @Getter
  @Setter
  @Indexed
  private long count;

}
