package com.smc.util;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class CombinationsTest {

  @Test
  public void test() throws Exception {
    Set<Integer> vals = set(1, 2, 3);

    Set<Set<Integer>> actual;
    Set<Set<Integer>> expected;

    actual = new Combinations<>(vals, 2, 2).find();
    expected = metaSet(set(1, 2), set(1, 3), set(2, 3));
    assertEquals(expected, actual);

    actual = new Combinations<>(vals, 0, 0).find();
    expected = metaSet();
    assertEquals(expected, actual);

    actual = new Combinations<>(vals, 1, 3).find();
    expected = metaSet(set(1), set(2), set(3), set(1, 2), set(1, 3), set(2, 3), set(1, 2, 3));
    assertEquals(expected, actual);

    actual = new Combinations<>(vals, 2, 3).find();
    expected = metaSet(set(1, 2), set(1, 3), set(2, 3), set(1, 2, 3));
    assertEquals(expected, actual);
  }

  @SafeVarargs
  private final Set<Set<Integer>> metaSet(Set<Integer>... sets) {
    return new HashSet<>(asList(sets));
  }

  private Set<Integer> set(Integer... ints) {
    return new HashSet<>(asList(ints));
  }


}
