package polaroid.client.scripts.lua.libraries;

import polaroid.client.scripts.interpreter.LuaValue;
import polaroid.client.scripts.interpreter.compiler.jse.CoerceJavaToLua;
import polaroid.client.scripts.interpreter.lib.OneArgFunction;
import polaroid.client.scripts.interpreter.lib.TwoArgFunction;
import polaroid.client.scripts.lua.classes.ModuleClass;

public class ModuleLibrary extends TwoArgFunction {

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("register", new register());

        env.set("module", library);
        return library;
    }

    public class register extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            return CoerceJavaToLua.coerce(new ModuleClass(arg.toString()));
        }

    }

}


