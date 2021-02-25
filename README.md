# panama-fastaccess

A prototype of a kind of middle ground API for panama foreign memory

The idea is to provide an API based on a C-like DSL to access to the content of a data structure
defined by a `MemoryLayout`

Here is the crux of the idea, first create an instance of `FastAccess` on a `MemoryLayout`
then profit :)
```java
  private static final FastAccess FAST_ACCESS = FastAccess.of(
      MemoryLayout.ofSequence(
        MemoryLayout.ofStruct(
            MemoryLayout.ofValueBits(32, nativeOrder()).withName("key"),
            MemoryLayout.ofValueBits(32, nativeOrder()).withName("value")
        )
    ));
    ...
    
    try (var segment = MemorySegment.allocateNative(400)) {
      
      // read a member of the array of struct
      FAST_ACCESS.getInt(segment, "[].key", 2);  // in C: segment[2].key
    
      // write a member of the array of struct
      FAST_ACCESS.setInt(segment, "[].value", 1, 84);  // in C: segment[1].value = 84
    }
```

The code of `FastAccess` is written in a such way that the JIT should be able to fully inline it,
thus be fast.
