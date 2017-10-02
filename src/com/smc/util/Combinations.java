package com.smc.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Generic class used to find combinations of values that lie within a given range of sizes. This is
 * achieved using a depth first search.
 *
 * @param <T>
 *
 * @author Stuart Clark
 */
public class Combinations<T> {

  private Node<T> root;
  private int minSize;

  /**
   * @param values the set of possible values.
   * @param minSize the minimum size a returned combination can be (inclusive).
   * @param maxSize the maximum size a returned combination can be (inclusive).
   */
  public Combinations(Set<T> values, int minSize, int maxSize) {
    this.minSize = minSize;
    this.root = new Node<>(null, new ArrayList<>(values), null, 0, maxSize);
  }

  /**
   * @return The set of all the combinations which have a size in the range of minSize-maxSize
   *         (inclusive).
   */
  public Set<Set<T>> find() {
    Set<Set<T>> combinations = new HashSet<>();
    find(root, combinations);
    return combinations;
  }

  /**
   * Recursive depth first search use to find the combinations.
   * 
   * @param node
   * @param combinations
   */
  private void find(Node<T> node, Set<Set<T>> combinations) {
    for (Node<T> child : node) {
      find(child, combinations);
      if (child.getDepth() >= minSize) {
        combinations.add(valuesSet(child));
      }
    }
  }

  /**
   * Traverses up the path from {@code node} to the root of the tree adding each {@link Node#value}
   * to the set that is returned.
   *
   * @param node
   * @return the set of value found along the path from the {@code node} to the root.
   */
  public Set<T> valuesSet(Node<T> node) {
    Set<T> values = new HashSet<>();

    while (node.getParent() != null) {
      values.add(node.getValue());
      node = node.getParent();
    }

    return values;
  }

}
