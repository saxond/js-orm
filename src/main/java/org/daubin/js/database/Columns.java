package org.daubin.js.database;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Column;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

class Columns {
    
    public static List<ColumnMetadata> createColumns(Map<String, Object> map) {
        List<ColumnMetadata> columns = Lists.newArrayListWithExpectedSize(map.size());
        for (Entry<String, Object> entry : map.entrySet()) {
            ColumnMetadata column = Columns.createColumn(entry);
            columns.add(column);
        }
        return columns;
    }

    @SuppressWarnings("unchecked")
	static ColumnMetadata createColumn(Entry<String, Object> entry) {
        final Map<String,Object> map;
        if (entry.getValue() instanceof ColumnType) {
            map = Maps.newHashMap(ImmutableMap.of("type", entry.getValue()));
        } else if (entry.getValue() instanceof Map) {
            map = (Map<String, Object>) entry.getValue();
        } else {
            throw new RuntimeException("Unable to parse column " + entry.getKey());
        }
        return build(entry.getKey(), map);
    }
    
    static class ColumnMetadata {
    	private final Column column;
    	private final ColumnType columnType;
		private final boolean id;
		private final boolean generatedValue;
    	
		public ColumnMetadata(Column column, ColumnType columnType, boolean id, boolean generatedValue) {
			this.column = column;
			this.columnType = columnType;
			this.id = id;
			this.generatedValue = generatedValue;
		}

		public Column getColumn() {
			return column;
		}

		public ColumnType getColumnType() {
			return columnType;
		}
		
		public boolean isId() {
			return id;
		}

		public boolean isGeneratedValue() {
			return generatedValue;
		}
    }
    
    public static ColumnMetadata build(
            final String name,
            final Map<String,Object> map) {
        
        final ColumnType type = (ColumnType) map.remove("type");
        
        Object idValue = map.remove("id");
        Object generatedValue = map.remove("generatedValue");
        
        Set<String> validKeys = Sets.newHashSet();
        for (Method m : Column.class.getDeclaredMethods()) {
        	validKeys.add(m.getName());
        }
        
        Set<String> keys = Sets.newHashSet(map.keySet());
        keys.removeAll(validKeys);
        if (!keys.isEmpty()) {
        	throw new RuntimeException("Column `" + name + "` references unsupported keys: " + keys);
        }
        
        map.put("name", name);
        final Column col = Annotations.generateAnnotationProxy(Column.class, map);
        
        return new ColumnMetadata(col, type, 
        		(idValue != null && (Boolean)idValue), 
        		(generatedValue != null && (Boolean)generatedValue));
    }

}
