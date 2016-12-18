package org.daubin.js.database;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.j256.ormlite.jdbc.JdbcConnectionSource;

import jdk.internal.dynalink.beans.StaticClass;
import jdk.nashorn.internal.objects.NativeJava;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.Source;

public class Initializer {

    private final static Map<String, Class<?>> GLOBALS = ImmutableMap.<String, Class<?>>builder().
            put("Type", ColumnType.class).
            put("ConnectionSource", JdbcConnectionSource.class).
            put("DBContext", DatabaseContext.class).
            put("Collectors", Collectors.class).
            build();
    
    private final static Map<Class<?>, String> HELP = ImmutableMap.<Class<?>, String>builder().
            put(ColumnType.class, "").
            put(JdbcConnectionSource.class, "").
            put(DatabaseContext.class, DatabaseContext.help()).
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
                print(help);
                return "";
            }
        }
        print("No help available for " + obj);
        return "";
    }
    
    private static void print(String message) {
        Context.getContext().getOut().println(message);
    }

    private static String getHelpText() {
        String help = "";
        return help;
    }

    static  void defineScript(String name, String script) throws Throwable {
        // compile the function
        ScriptFunction function = Context.getContext().compileScript(Source.sourceFor(name, script), Context.getGlobal());
        
        // invoke it to define the function
        final MethodHandle invokeHandle = function.getBoundInvokeHandle(function);
        invokeHandle.invoke();
    }
}
