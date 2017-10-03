package com.smc.model;

import lombok.ToString;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import com.smc.Analysis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Model used to represent the result of aggregations that count the number of occurrences of
 * {@code string}. See {@link Analysis}.
 *
 * @author Stuart Clark
 */
@EqualsAndHashCode()
public abstract class StringCountResult {

  @Id
  @Getter
  @Setter
  private String string;

  @Getter
  @Setter
  @Indexed
  private long count;

}
