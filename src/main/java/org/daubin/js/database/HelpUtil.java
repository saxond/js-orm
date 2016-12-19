package org.daubin.js.database;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

class HelpUtil {
    private HelpUtil() { }

    public static StringBuilder appendMethods(StringBuilder builder, Method[] methods) {
        builder.append("Methods:\n");
        for (Method m : methods) {
            builder.append('\t').
                append(m.getReturnType().getTypeName()).
                append(' ').
                append(m.getName()).
                append('(').
                append(Joiner.on(',').join(
                        ImmutableList.copyOf(m.getParameters()).stream().
                        map(p -> p.getType().getName()).collect(Collectors.toList()))).
                append(')').
                append('\n');
        }
        return builder;
    }

    public static StringBuilder appendFields(StringBuilder builder, Field[] fields) {
        builder.append("Instance Fields:\n");
        for (Field f : fields) {
            builder.append('\t').append(f.getName()).append('\n');
        }
        return builder;
    }
}
