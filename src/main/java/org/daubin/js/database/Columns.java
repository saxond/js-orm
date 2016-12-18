package org.daubin.js.database;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

class Columns {
    
    public static List<ColumnAndType> createColumns(Map<String, Object> map) {
        List<ColumnAndType> columns = Lists.newArrayListWithExpectedSize(map.size());
        for (Entry<String, Object> entry : map.entrySet()) {
            ColumnAndType column = Columns.createColumn(entry);
            columns.add(column);
        }
        return columns;
    }

    @SuppressWarnings("unchecked")
	static ColumnAndType createColumn(Entry<String, Object> entry) {
        final Map<String,Object> map;
        if (entry.getValue() instanceof ColumnType) {
            map = ImmutableMap.of("type", entry.getValue());
        } else if (entry.getValue() instanceof Map) {
            map = (Map<String, Object>) entry.getValue();
        } else {
            throw new RuntimeException("Unable to parse column " + entry.getKey());
        }
        return build(entry.getKey(), map);
    }
    
    interface ColumnAndType extends Column {
        ColumnType type();
    }
    
    private static boolean asBoolean(Object value, boolean defaultValue) {
        return value instanceof Boolean ? (Boolean)value : defaultValue;
    }
    
    private static String asString(Object value, String defaultValue) {
        return value instanceof String ? (String)value : defaultValue;
    }
    
    private static int asInteger(Object value, int defaultValue) {
        return value instanceof Number ? ((Number)value).intValue() : defaultValue;
    }
    
    public static ColumnAndType build(
            final String name, 
            final Map<String,Object> map) {
        
        final ColumnType type = (ColumnType) map.get("type");
        
        return new ColumnAndType() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public boolean unique() {
                return asBoolean(map.get("unique"), false);
            }

            @Override
            public boolean nullable() {
                return asBoolean(map.get("nullable"), true);
            }

            @Override
            public boolean insertable() {
                return asBoolean(map.get("insertable"), true);
            }

            @Override
            public boolean updatable() {
                return asBoolean(map.get("updatable"), true);
            }

            @Override
            public String columnDefinition() {
                return asString(map.get("columnDefinition"), "");
            }

            @Override
            public String table() {
                return "";
            }

            @Override
            public int length() {
                return asInteger(map.get("length"), 255);
            }

            @Override
            public int precision() {
                return asInteger(map.get("precision"), 0);
            }

            @Override
            public int scale() {
                return asInteger(map.get("scale"), 0);
            }

            @Override
            public ColumnType type() {
                return type;
            }
        };
    }
}
