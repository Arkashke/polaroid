package polaroid.client.scripts.interpreter.globals;

import polaroid.client.scripts.interpreter.compiler.LuaC;
import polaroid.client.scripts.interpreter.Globals;
import polaroid.client.scripts.interpreter.LoadState;
import polaroid.client.scripts.interpreter.lib.*;
import polaroid.client.scripts.lua.libraries.ModuleLibrary;
import polaroid.client.scripts.lua.libraries.PlayerLibrary;

public class Standarts {
    public static Globals standardGlobals() {
        Globals globals = new Globals();
        globals.load(new BaseLib());
        globals.load(new Bit32Lib());
        globals.load(new MathLib());
        globals.load(new TableLib());
        globals.load(new StringLib());
        globals.load(new PlayerLibrary());
        globals.load(new ModuleLibrary());
        LoadState.install(globals);
        LuaC.install(globals);
        return globals;
    }
}


