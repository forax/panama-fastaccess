package com.github.forax.panama.fastaccess;

import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;

public interface FastAccess {
  int getInt(MemorySegment segment, String path);
  int getInt(MemorySegment segment, String path, long index0);
  int getInt(MemorySegment segment, String path, long index0, long index1);

  void setInt(MemorySegment segment, String path, int value);
  void setInt(MemorySegment segment, String path, long index0, int value);
  void setInt(MemorySegment segment, String path, long index0, long index1, int value);

  static FastAccess of(MemoryLayout layout) {
    return FastAccessImpl.getImpl(layout);
  }
}
