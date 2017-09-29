package com.smc.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Used to store {@link Collection}s of words in a manner that makes them easily searchable.
 */
@EqualsAndHashCode
@ToString
public class Phrase {

  @Getter
  private int size;

  @Getter
  private String text;

  /**
   * {@code words} is sorted and stored as a single string to reduce the time taken to perform
   * mongodb aggregations.
   *
   * @param words
   */
  public Phrase(Collection<String> words) {
    List<String> wordsList = new ArrayList<>(words);
    Collections.sort(wordsList);
    this.text = String.join(" ", wordsList);
    this.size = words.size();
  }

}
