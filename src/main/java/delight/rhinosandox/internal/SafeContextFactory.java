package delight.rhinosandox.internal;

import delight.rhinosandox.exceptions.ScriptCPUAbuseException;
import delight.rhinosandox.exceptions.ScriptDurationException;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

/**
 * see http://www-archive.mozilla.org/rhino/apidocs/org/mozilla/javascript/ContextFactory.html
 * 
 * Also see https://github.com/flozano/rhino-sandbox-test/blob/master/src/main/java/com/flozano/rhino/sandbox/SandboxContextFactory.java
 */
@SuppressWarnings("all")
public class SafeContextFactory extends ContextFactory {
  public static class CountContext extends Context {
    private long startTime;
    
    private long instructions;
  }
  
  public long maxRuntimeInMs;
  public int maxInstructions;

  private int instructionSteps = 10000;

  public SafeContextFactory() {
    super();
  }

  public SafeContextFactory(int instructionSteps) {
    super();
    this.instructionSteps = instructionSteps;
  }

  @Override
  public Context makeContext() {
    final SafeContextFactory.CountContext cx = new SafeContextFactory.CountContext();
    cx.setOptimizationLevel((-1));
    cx.setInstructionObserverThreshold(instructionSteps);
    return cx;
  }

  @Override
  public boolean hasFeature(final Context cx, final int featureIndex) {
    switch (featureIndex) {
      case Context.FEATURE_NON_ECMA_GET_YEAR:
        return true;
      case Context.FEATURE_MEMBER_EXPR_AS_FUNCTION_NAME:
        return true;
      case Context.FEATURE_RESERVED_KEYWORD_AS_IDENTIFIER:
        return true;
      case Context.FEATURE_PARENT_PROTO_PROPERTIES:
        return false;
    }
    return super.hasFeature(cx, featureIndex);
  }
  
  @Override
  public void observeInstructionCount(final Context cx, final int instructionCount) {
    try {
      final SafeContextFactory.CountContext mcx = ((SafeContextFactory.CountContext) cx);
      final long currentTime = System.currentTimeMillis();
      if (((this.maxRuntimeInMs > 0) && ((currentTime - mcx.startTime) > this.maxRuntimeInMs))) {
        throw new ScriptDurationException();
      }
      mcx.instructions = (mcx.instructions + mcx.getInstructionObserverThreshold());
      if (((this.maxInstructions > 0) && (mcx.instructions > this.maxInstructions))) {
        throw new ScriptCPUAbuseException();
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public Object doTopCall(final Callable callable, final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
    final SafeContextFactory.CountContext mcx = ((SafeContextFactory.CountContext) cx);
    mcx.startTime = System.currentTimeMillis();
    mcx.instructions = 0;
    return super.doTopCall(callable, cx, scope, thisObj, args);
  }
}
