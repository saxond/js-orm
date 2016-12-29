package org.daubin.js.database;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jdk.internal.dynalink.beans.StaticClass;
import jdk.nashorn.internal.objects.NativeJava;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.Source;

import com.google.common.collect.ImmutableMap;

public class Initializer {

    private final static Map<String, Class<?>> GLOBALS = ImmutableMap.<String, Class<?>>builder().
            put("Type", ColumnType.class).
            put("EMBuilder", EntityManagerFactoryBuilder.class).
            put("Collectors", Collectors.class).
            build();
    
    private final static Map<Class<?>, String> HELP = ImmutableMap.<Class<?>, String>builder().
            put(ColumnType.class, "").
            put(EntityManagerFactoryBuilder.class, EntityManagerFactoryBuilder.help()).
            put(Collectors.class, "").
            build();
    
    private Initializer() {}

    public static void init() throws Throwable {
        
        for (Entry<String, Class<?>> entry : GLOBALS.entrySet()) {
            Context.getGlobal().addOwnProperty(entry.getKey(), 0, NativeJava.type(null, entry.getValue().getName()));
            Context.getContext().getOut().println("Registered " + entry.getKey());
        }
        //Context.getGlobal().set("help", getHelpText(), true);
        Context.getContext().getOut().println("Registered help");
        
        defineScript("utils", "function help(obj) { " + Initializer.class.getName() + ".help(obj); }");
    }
    
    public static String help(Object obj) {
        if (obj instanceof StaticClass) {
            Class<?> clazz = ((StaticClass)obj).getRepresentedClass();
            String help = HELP.get(clazz);
            if (null != help) {
                return help;
            }
        }
        return "No help available for " + obj;
    }

    static  void defineScript(String name, String script) throws Throwable {
        // compile the function
        ScriptFunction function = Context.getContext().compileScript(Source.sourceFor(name, script), Context.getGlobal());
        
        // invoke it to define the function
        final MethodHandle invokeHandle = function.getBoundInvokeHandle(function);
        invokeHandle.invoke();
    }
}
