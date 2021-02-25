package com.github.forax.panama.fastaccess;

import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.foreign.MemorySegment;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.VarHandle.AccessMode;
import java.util.ArrayList;
import java.util.List;

import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;

record FastAccessImpl(Kind kind) implements FastAccess {
  private record Kind(MethodHandle getInt, MethodHandle setInt) { }

  @Override
  public int getInt(MemorySegment segment, String path) {
    try {
      return (int) kind.getInt.invokeExact(path, 0, segment, -1L, -1L);
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable t) {
      throw new AssertionError(t);
    }
  }

  @Override
  public int getInt(MemorySegment segment, String path, long index0) {
    try {
      return (int) kind.getInt.invokeExact(path, 1, segment, index0, -1L);
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable t) {
      throw new AssertionError(t);
    }
  }

  @Override
  public int getInt(MemorySegment segment, String path, long index0, long index1) {
    try {
      return (int) kind.getInt.invokeExact(path, 2, segment, index0, index1);
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable t) {
      throw new AssertionError(t);
    }
  }

  @Override
  public void setInt(MemorySegment segment, String path, int value) {
    try {
      kind.setInt.invokeExact(path, 0, segment, value, -1L, -1L);
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable t) {
      throw new AssertionError(t);
    }
  }

  @Override
  public void setInt(MemorySegment segment, String path, long index0, int value) {
    try {
      kind.setInt.invokeExact(path, 1, segment, value, index0, -1L);
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable t) {
      throw new AssertionError(t);
    }
  }

  @Override
  public void setInt(MemorySegment segment, String path, long index0, long index1, int value) {
    try {
      kind.setInt.invokeExact(path, 0, segment, value, index0, index1);
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable t) {
      throw new AssertionError(t);
    }
  }

  static FastAccess getImpl(MemoryLayout layout) {
    var kind = new Kind(getIntMH(layout), setIntMH(layout));
    return new FastAccessImpl(kind);
  }

  private static MethodHandle getIntMH(MemoryLayout layout) {
    return new InliningCache(int.class, AccessMode.GET, layout).dynamicInvoker();
  }

  private static MethodHandle setIntMH(MemoryLayout layout) {
    return new InliningCache(int.class, AccessMode.SET, layout).dynamicInvoker();
  }

  private static class InliningCache extends MutableCallSite {
    private static final MethodHandle ERASED_GET, ERASED_SET, SAME_SHAPE;

    static {
      var lookup = MethodHandles.lookup();
      try {
        ERASED_GET = lookup.findVirtual(InliningCache.class, "erasedGet",
            methodType(Object.class, String.class, int.class, MemorySegment.class, long[].class));
        ERASED_SET = lookup.findVirtual(InliningCache.class, "erasedSet",
            methodType(void.class, String.class, int.class, MemorySegment.class, Object.class, long[].class));
        SAME_SHAPE = lookup.findStatic(InliningCache.class, "sameShape",
            methodType(boolean.class, String.class, int.class, String.class, int.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }

    private final Class<?> carrier;
    private final AccessMode accessMode;
    private final MemoryLayout layout;

    private InliningCache(Class<?> carrier, AccessMode accessMode, MemoryLayout layout) {
      super(typeFromAccessMode(carrier, accessMode));
      this.carrier = carrier;
      this.accessMode = accessMode;
      this.layout = layout;
      setTarget(fallback(accessMode).bindTo(this).asCollector(long[].class, 2).asType(type()));
    }

    private static MethodType typeFromAccessMode(Class<?> carrier,AccessMode accessMode) {
      return switch(accessMode) {
        case GET -> methodType(carrier, String.class, int.class, MemorySegment.class, long.class, long.class);
        case SET -> methodType(void.class, String.class, int.class, MemorySegment.class, carrier, long.class, long.class);
        default -> throw new AssertionError("invalid access mode " + accessMode);
      };
    }

    private static MethodHandle fallback(AccessMode accessMode) {
      return switch (accessMode) {
        case GET -> ERASED_GET;
        case SET -> ERASED_SET;
        default -> throw new AssertionError("invalid access mode " + accessMode);
      };
    }

    private static boolean sameShape(String path, int arity, String expectedPath, int expectedArity) {
      //noinspection StringEquality
      return path == expectedPath && arity == expectedArity;
    }

    private Object erasedGet(String path, int arity, MemorySegment segment, long[] indexes) throws Throwable {
      requireNonNull(segment, "segment is null");
      requireNonNull(path, "path is null");

      //noinspection StringEquality
      if (path != path.intern()) {
        throw new IllegalArgumentException("path " + path + " is not a constant string");
      }

      var pathElements = parsePath(path);
      var varHandle = layout.varHandle(carrier, pathElements.toArray(PathElement[]::new));
      var mh = varHandle.toMethodHandle(AccessMode.GET);

      var parameterCount = mh.type().parameterCount();
      if (arity != parameterCount - 1) {
        throw new IllegalStateException("path arity " + (parameterCount - 1) + " does not match method arity " + arity);
      }

      if (parameterCount != 3) {
        mh = dropArguments(mh, parameterCount, range(0, 3 - parameterCount).mapToObj(__ -> long.class).toArray(Class[]::new));
      }

      var result = mh.asSpreader(long[].class, 2).invoke(segment, indexes);

      var guard = guardWithTest(
          insertArguments(SAME_SHAPE, 2, path, arity),
          dropArguments(mh, 0, String.class, int.class),
          new InliningCache(carrier, accessMode, layout).dynamicInvoker());
      setTarget(guard);

      return result;
    }

    private void erasedSet(String path, int arity, MemorySegment segment, Object value, long[] indexes) throws Throwable {
      requireNonNull(segment, "segment is null");
      requireNonNull(path, "path is null");

      //noinspection StringEquality
      if (path != path.intern()) {
        throw new IllegalArgumentException("path " + path + " is not a constant string");
      }

      var pathElements = parsePath(path);
      var varHandle = layout.varHandle(carrier, pathElements.toArray(PathElement[]::new));
      var mh = varHandle.toMethodHandle(AccessMode.SET);

      var parameterCount = mh.type().parameterCount();
      if (arity != parameterCount - 2) {
        throw new IllegalStateException("path arity " + (parameterCount - 2) + " does not match method arity " + arity);
      }

      if (parameterCount != 4) {
        mh = dropArguments(mh, parameterCount - 1, range(0, 4 - mh.type().parameterCount()).mapToObj(__ -> long.class).toArray(Class[]::new));
      }

      mh = MethodHandles.permuteArguments(mh,
          methodType(void.class, MemorySegment.class, int.class, long.class, long.class),
          0, 2, 3, 1);

      mh.asSpreader(long[].class, 2).invoke(segment, value, indexes);

      var guard = guardWithTest(
          insertArguments(SAME_SHAPE, 2, path, arity),
          dropArguments(mh, 0, String.class, int.class),
          new InliningCache(carrier, accessMode, layout).dynamicInvoker());
      setTarget(guard);
    }
  }

  private static class Lexer {
    private enum Token { IDENTIFIER, ARRAY, END }

    private final String path;
    String text;
    private int offset;

    public Lexer(String path) {
      this.path = path;
    }

    public Token nextToken() {
      if (offset == path.length()) {
        return Token.END;
      }
      var c = path.charAt(offset++);
      if (c == '.') {
        nextIdentifier();
        return Token.IDENTIFIER;
      }
      if (c == '[') {
        offset++;
        return Token.ARRAY;
      }
      throw new IllegalArgumentException("path parsing: invalid character '" + c + "' in " + path);
    }

    private void nextIdentifier() {
      var builder = new StringBuilder();
      for(; offset < path.length(); offset++) {
        var c = path.charAt(offset);
        if (c == '.' || c == '[') {
          break;
        }
        builder.append(c);
      }
      text = builder.toString();
    }
  }

  private static List<PathElement> parsePath(String path) {
    var lexer = new Lexer(path);
    var pathElements = new ArrayList<PathElement>();
    for(;;) {
      var token = lexer.nextToken();
      switch(token) {
        case IDENTIFIER -> pathElements.add(PathElement.groupElement(lexer.text));
        case ARRAY -> pathElements.add(PathElement.sequenceElement());
        case END -> { return pathElements; }
      }
    }
  }
}
