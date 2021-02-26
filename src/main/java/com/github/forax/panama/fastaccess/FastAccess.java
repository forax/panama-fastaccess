package com.github.forax.panama.fastaccess;

import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;

import java.util.Objects;

/**
 * A simple API to read/write the value of a {@link MemorySegment} in a structured way from a {@link MemoryLayout}.
 *
 * This API can access through a {@code path} composed of
 * <ul>
 *   <li>field using {@code .member} with {@code member} being the name defined in the {@link MemoryLayout}
 *   <li>array using {@code []}
 * </ul>
 *
 * An example with a simple struct
 * <pre>
 *   private static final FastAccess FAST_ACCESS;
 *   static {
 *     var struct = MemoryLayout.ofStruct(
 *       MemoryLayout.ofValueBits(32, nativeOrder()).withName("key"),
 *       MemoryLayout.ofValueBits(32, nativeOrder()).withName("value")
 *     );
 *     FAST_ACCESS = FastAccess.of(struct);
 *   }
 *   ...
 *   try (var segment = MemorySegment.allocateNative(400)) {
 *     fastAccess.setInt(segment, ".key", 777);
 *     fastAccess.setInt(segment, ".value", 333);
 *
 *     assertEquals(777, fastAccess.getInt(segment, ".key"));
 *     assertEquals(333, fastAccess.getInt(segment, ".value"));
 *   }
 * </pre>
 *
 * A more complex example with an array of structs
 * <pre>
 *   private static final FastAccess FAST_ACCESS;
 *   static {
 *     var arrayOfStruct = MemoryLayout.ofSequence(
 *         MemoryLayout.ofStruct(
 *             MemoryLayout.ofValueBits(32, nativeOrder()).withName("key"),
 *             MemoryLayout.ofValueBits(32, nativeOrder()).withName("value")
 *         )
 *     );
 *     FAST_ACCESS = FastAccess.of(arrayOfStruct);
 *  }
 *  ...
 *  try (var segment = MemorySegment.allocateNative(400)) {
 *    fastAccess.setInt(segment, "[].key", 5, 777);
 *    fastAccess.setInt(segment, "[].value", 5, 333);
 *
 *    assertEquals(777, fastAccess.getInt(segment, "[].key", 5));
 *    assertEquals(333, fastAccess.getInt(segment, "[].value", 5));
 *  }
 * </pre>
 */
public interface FastAccess {
  /**
   * Read an int from the {@code segment} at the position specified by the {@code path}.
   *
   * @param segment the current segment
   * @param path the path to the struct member
   * @return the int value at the position specified by the {@code path}
   *
   * @throws NullPointerException is either the {@code segment} or the {@code path} is null
   * @throws IllegalArgumentException if the path is not a constant string or if the syntax of the path
   *         is invalid or if the layout path does not select a value layout
   *         (see {@link jdk.incubator.foreign.ValueLayout}) or if the selected value layout has a size
   *         that that does not match that of the size of an int.
   * @throws IllegalStateException if the path as more than zero {@code []}
   */
  int getInt(MemorySegment segment, String path);

  /**
   * Read an int from the {@code segment} at the position specified by the {@code path} and the index {@code index0}.
   *
   * @param segment the current segment
   * @param path the path to the struct member
   * @param index0 the index that will be inserted inside the {@code []} specified by the path
   * @return the int value at the position specified by the {@code path} and the index {@code index0}
   *
   * @throws NullPointerException is either the {@code segment} or the {@code path} is null
   * @throws IllegalArgumentException if the path is not a constant string or if the syntax of the path
   *         is invalid or if the layout path does not select a value layout
   *         (see {@link jdk.incubator.foreign.ValueLayout}) or if the selected value layout has a size
   *         that that does not match that of the size of an int.
   * @throws IllegalStateException if the path as more or less than one {@code []}
   */
  int getInt(MemorySegment segment, String path, long index0);

  /**
   * Read an int from the {@code segment} at the position specified by the {@code path} and the indexes
   * {@code index0} and {@code index1}.
   *
   * @param segment the current segment
   * @param path the path to the struct member
   * @param index0 the index that will be inserted inside the first {@code []} specified by the path
   * @param index1 the index that will be inserted inside the second {@code []} specified by the path
   * @return the int value at the position specified by the {@code path} and the index {@code index0}
   *
   * @throws NullPointerException is either the {@code segment} or the {@code path} is null
   * @throws IllegalArgumentException if the path is not a constant string or if the syntax of the path
   *         is invalid or if the layout path does not select a value layout
   *         (see {@link jdk.incubator.foreign.ValueLayout}) or if the selected value layout has a size
   *         that that does not match that of the size of an int.
   * @throws IllegalStateException if the path as more or less than two {@code []}
   */
  int getInt(MemorySegment segment, String path, long index0, long index1);

  /**
   * Read an int from the {@code segment} at the position specified by the {@code path}.
   *
   * @param segment the current segment
   * @param path the path to the struct member
   * @param value the int value to write
   *
   * @throws NullPointerException is either the {@code segment} or the {@code path} is null
   * @throws IllegalArgumentException if the path is not a constant string or if the syntax of the path
   *         is invalid or if the layout path does not select a value layout
   *         (see {@link jdk.incubator.foreign.ValueLayout}) or if the selected value layout has a size
   *         that that does not match that of the size of an int.
   * @throws IllegalStateException if the path as more than zero {@code []}
   */
  void setInt(MemorySegment segment, String path, int value);

  /**
   * Write an int from the {@code segment} at the position specified by the {@code path} and the index {@code index0}.
   *
   * @param segment the current segment
   * @param path the path to the struct member
   * @param index0 the index that will be inserted inside the {@code []} specified by the path
   * @param value the int value to write
   *
   * @throws NullPointerException is either the {@code segment} or the {@code path} is null
   * @throws IllegalArgumentException if the path is not a constant string or if the syntax of the path
   *         is invalid or if the layout path does not select a value layout
   *         (see {@link jdk.incubator.foreign.ValueLayout}) or if the selected value layout has a size
   *         that that does not match that of the size of an int.
   * @throws IllegalStateException if the path as more or less than one {@code []}
   */
  void setInt(MemorySegment segment, String path, long index0, int value);

  /**
   * Write an int from the {@code segment} at the position specified by the {@code path} and the indexes
   * {@code index0} and {@code index1}.
   *
   * @param segment the current segment
   * @param path the path to the struct member
   * @param index0 the index that will be inserted inside the first {@code []} specified by the path
   * @param index1 the index that will be inserted inside the second {@code []} specified by the path
   * @param value the int value to write
   *
   * @throws NullPointerException is either the {@code segment} or the {@code path} is null
   * @throws IllegalArgumentException if the path is not a constant string or if the syntax of the path
   *         is invalid or if the layout path does not select a value layout
   *         (see {@link jdk.incubator.foreign.ValueLayout}) or if the selected value layout has a size
   *         that that does not match that of the size of an int.
   * @throws IllegalStateException if the path as more or less than two {@code []}
   */
  void setInt(MemorySegment segment, String path, long index0, long index1, int value);

  /**
   * Creates a {@code FastAccess} instance from a {@link MemoryLayout}.
   *
   * In order to be fast, the {@code FastAccess} instance should always be stored in a static final field
   * <pre>
   *  private static final FastAccess FAST_ACCESS;
   *  static {
   *    MemoryLayout layout = ...
   *    FAST_ACCESS = FastAccess.of(layout);
   *  }
   * </pre>
   *
   * @param layout the memory layout used to specify the access patterns
   * @return a new {@code FastAccess} instance
   *
   * @throws NullPointerException if the layout is null
   */
  static FastAccess of(MemoryLayout layout) {
    Objects.requireNonNull(layout, "layout is null");
    return FastAccessImpl.getImpl(layout);
  }
}
