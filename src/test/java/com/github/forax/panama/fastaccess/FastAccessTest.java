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
  public void getInt() {
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
  public void setInt() {
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

      for (int i = 0 ; i < 100 ; i++) {
        MemoryAccess.setIntAtIndex(segment, i, i);
      }
    }
  }


}