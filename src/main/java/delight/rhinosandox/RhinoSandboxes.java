package delight.rhinosandox;

import delight.rhinosandox.RhinoSandbox;
import delight.rhinosandox.internal.RhinoSandboxImpl;

@SuppressWarnings("all")
public class RhinoSandboxes {
  public static RhinoSandbox create() {
    return new RhinoSandboxImpl(10000);
  }

  public static RhinoSandbox create(int instructionSteps) {
    return new RhinoSandboxImpl(instructionSteps);
  }
}
