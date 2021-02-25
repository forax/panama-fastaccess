package com.github.forax.panama.fastaccess;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SequenceLayout;
import org.junit.jupiter.api.Test;

import static java.nio.ByteOrder.nativeOrder;
import static org.junit.jupiter.api.Assertions.*;

public class FastAccessTest {

  @Test
  public void getIntArray() {
    var array = MemoryLayout.ofSequence(
        MemoryLayout.ofValueBits(32, nativeOrder())
    );

    var fastAccess = FastAccess.of(array);
    try (var segment = MemorySegment.allocateNative(400)) {
      for (int i = 0 ; i < 100 ; i++) {
        MemoryAccess.setIntAtIndex(segment, i, i);
      }

      for (int i = 0 ; i < 100 ; i++) {
        assertEquals(i, fastAccess.getInt(segment, "[]", i));
      }
    }
  }

  @Test
  public void setIntArray() {
    var array = MemoryLayout.ofSequence(
        MemoryLayout.ofValueBits(32, nativeOrder())
    );

    var fastAccess = FastAccess.of(array);
    try (var segment = MemorySegment.allocateNative(400)) {
      for (int i = 0 ; i < 100 ; i++) {
        fastAccess.setInt(segment, "[]", i, i);
      }

      for (int i = 0 ; i < 100 ; i++) {
        assertEquals(i, MemoryAccess.getIntAtIndex(segment, i));
      }
    }
  }

  @Test
  public void getIntStruct() {
    var struct = MemoryLayout.ofStruct(
            MemoryLayout.ofValueBits(32, nativeOrder()).withName("key"),
            MemoryLayout.ofValueBits(32, nativeOrder()).withName("value")
        );

    var fastAccess = FastAccess.of(struct);
    try (var segment = MemorySegment.allocateNative(400)) {
      MemoryAccess.setIntAtIndex(segment, 0, 777);
      MemoryAccess.setIntAtIndex(segment, 1, 333);

      assertEquals(777, fastAccess.getInt(segment, ".key"));
      assertEquals(333, fastAccess.getInt(segment, ".value"));
    }
  }

  @Test
  public void setIntStruct() {
    var struct = MemoryLayout.ofStruct(
        MemoryLayout.ofValueBits(32, nativeOrder()).withName("key"),
        MemoryLayout.ofValueBits(32, nativeOrder()).withName("value")
    );

    var fastAccess = FastAccess.of(struct);
    try (var segment = MemorySegment.allocateNative(400)) {
      fastAccess.setInt(segment, ".key", 777);
      fastAccess.setInt(segment, ".value", 333);

      assertEquals(777, MemoryAccess.getIntAtIndex(segment, 0));
      assertEquals(333, MemoryAccess.getIntAtIndex(segment, 1));
    }
  }

  @Test
  public void getIntArrayOfStruct() {
    SequenceLayout keyValues = MemoryLayout.ofSequence(
        MemoryLayout.ofStruct(
            MemoryLayout.ofValueBits(32, nativeOrder()).withName("key"),
            MemoryLayout.ofValueBits(32, nativeOrder()).withName("value")
        )
    ).withName("KeyValues");

    var fastAccess = FastAccess.of(keyValues);
    try (var segment = MemorySegment.allocateNative(400)) {
      for (int i = 0 ; i < 100 ; i++) {
        MemoryAccess.setIntAtIndex(segment, i, i);
      }

      assertEquals(4, fastAccess.getInt(segment, "[].key", 2));
      assertEquals(3, fastAccess.getInt(segment, "[].value", 1));
    }
  }

  @Test
  public void setIntArrayOfStruct() {
    SequenceLayout keyValues = MemoryLayout.ofSequence(
        MemoryLayout.ofStruct(
            MemoryLayout.ofValueBits(32, nativeOrder()).withName("key"),
            MemoryLayout.ofValueBits(32, nativeOrder()).withName("value")
        )
    ).withName("KeyValues");

    var fastAccess = FastAccess.of(keyValues);
    try (var segment = MemorySegment.allocateNative(400)) {
      fastAccess.setInt(segment, "[].key", 1, 42);
      fastAccess.setInt(segment, "[].value", 2, 84);

      assertEquals(42, MemoryAccess.getIntAtIndex(segment, 2));
      assertEquals(84, MemoryAccess.getIntAtIndex(segment, 5));
    }
  }


}