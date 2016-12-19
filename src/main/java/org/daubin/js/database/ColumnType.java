package org.daubin.js.database;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;

public enum ColumnType {
	
	/*
	LONG_STRING,
	BLOB,
	BIG_DECIMAL,
	 */
	
	BYTE_ARRAY(byte[].class, Byte.class),
    STRING(String.class),
    DATE(Date.class),
    SERIALIZABLE(Serializable.class),
    BOOLEAN(boolean.class, Boolean.class),
    CHAR(char.class, Character.class),
    BYTE(byte.class, Byte.class),
    DOUBLE(double.class, Double.class),
    FLOAT(float.class, Float.class),
    SHORT(short.class, Short.class),
    INTEGER(int.class, Integer.class),
    LONG(long.class, Long.class);
    
    protected final Class<?> primitiveClass;
    protected final Class<?> objectClass;

    private ColumnType(Class<?> primitiveClass) {
        this.primitiveClass = primitiveClass;
        this.objectClass = primitiveClass;
    }
    
	private ColumnType(Class<?> primitiveClass, Class<?> objectClass) {
		this.primitiveClass = primitiveClass;
		this.objectClass = objectClass;
	}

	public Class<?> getColumnClass(Column column) {
		return column.nullable() ? objectClass : primitiveClass;
	}
}
