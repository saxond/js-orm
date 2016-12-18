package org.daubin.js.database;

public enum ColumnType {
    String(String.class),
    Integer(int.class),
    Long(long.class);
    
    Class<?> clazz;

    ColumnType(Class<?> clazz) {
        this.clazz = clazz;
    }
}
