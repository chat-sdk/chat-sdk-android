package sdk.chat.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sdk.chat.core.module.Module;

public abstract class QuickStart {

    protected static boolean containsModule(Module module, Module... modules) {
        for (Module mod : modules) {
            if (mod.getName().equals(module.getName())) {
                return true;
            }
        }
        return false;
    }

    protected static List<Module> deduplicate(List<Module> lowPriority, Module... highPriority) {

        if (highPriority == null) {
            return lowPriority;
        }

        List<Module> modules = new ArrayList<>();

        for (Module m : lowPriority) {
            if (!containsModule(m, highPriority)) {
                modules.add(m);
            }
        }

        modules.addAll(Arrays.asList(highPriority));

        return modules;
    }

}