package com.smc.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lombok.Getter;

/**
 * Generic class used to represent a node in a tree used to find combinations. The node generates
 * it's own children as and when they are required.
 * 
 * @param <T>
 */
public class Node<T> implements Iterable<Node<T>> {

  @Getter
  private final T value;

  @Getter
  private final Node<T> parent;

  @Getter
  private final int depth;

  /**
   * The depth that {@code this} nodes children will be at (if it has any).
   */
  private final int nextDepth;

  private final int maxDepth;

  private Iterator<Node<T>> children;

  private List<T> childValues;

  public Node(T value, List<T> childValues, Node<T> parent, int depth, int maxDepth) {
    this.value = value;
    this.childValues = childValues;
    this.parent = parent;
    this.depth = depth;
    this.nextDepth = depth + 1;
    this.maxDepth = maxDepth;
  }

  @Override
  public Iterator<Node<T>> iterator() {
    return getChildren();
  }

  /**
   * @return lazily instantiated children or an empty {@link Iterator} if the max depth has been
   *         exceeded.
   */
  private Iterator<Node<T>> getChildren() {
    if (children == null) {
      if (nextDepth <= maxDepth) {
        this.children = createChildren();
      } else {
        this.children = Collections.emptyIterator();
      }
    }

    return children;
  }

  /**
   * @return an {@link Iterator} containing all of the child nodes for {@code this}.
   */
  private Iterator<Node<T>> createChildren() {
    List<Node<T>> children = new ArrayList<>();
    int numChildren = childValues.size();

    // Iterate over each of the child values
    for (int i = 0; i < numChildren; i++) {

      // Select the value for the child
      T childVal = childValues.get(i);

      // Create the list of remaining values
      List<T> grandChildVals = new ArrayList<>(numChildren - 1);
      for (int j = 0; j < numChildren; j++) {
        if (j != i) {
          grandChildVals.add(childValues.get(j));
        }
      }

      // Add the child to the list
      children.add(new Node<>(childVal, grandChildVals, this, nextDepth, maxDepth));

    }

    return children.iterator();
  }

}
