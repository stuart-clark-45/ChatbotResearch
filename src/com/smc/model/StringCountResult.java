package com.smc.model;

import org.mongodb.morphia.annotations.Id;

import com.smc.Analysis;

import lombok.Getter;
import org.mongodb.morphia.annotations.Indexed;

/**
 * Model used to represent the result of aggregations that count the number of occurrences of
 * {@code string}. See {@link Analysis}.
 *
 * @author Stuart Clark
 */
public abstract class StringCountResult {

  @Id
  @Getter
  private String string;

  @Getter
  @Indexed
  private long count;

}
