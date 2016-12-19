package org.daubin.js.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

import org.daubin.js.database.Columns.ColumnMetadata;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class DatabaseContext {
    
    private final ConnectionSource connection;
    private final ClassGenerator classGenerator;
    
    public DatabaseContext(ConnectionSource connection) throws NoSuchMethodException, SecurityException {
        this.connection = connection;
        this.classGenerator = new ClassGenerator();
    }

    /**
     * Creates a new DAO for the given table.  The map should be a map of column names to either
     * a {@link ColumnType} or another map of settings that can populate a {@link Column} instance.
     * 
     * Under the covers, this method generates a class to represent the table model with javax.persistence
     * annotations on the class and its fields.  The generated class is then fed into a Java ORM implementation.
     */
    public JsEntityManager define(String tableName, Map<String,Object> map) throws SQLException {
        
        List<ColumnMetadata> columns = Columns.createColumns(map);
        
        Class<?> clazz = classGenerator.createClass(tableName, columns);
        
        @SuppressWarnings("unchecked")
		Dao<Model, ?> dao = (Dao<Model, ?>) DaoManager.createDao(connection, clazz);
        return new JsEntityManager(dao, clazz);
    }

    public static String help() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Creation:\n");
        builder.append("\tvar context = new DBContext(conn)\n\n");
        
        builder.append("Usage:\n");
        builder.append("\tvar Account = context.define('accounts', {id: {type: Type.Integer, unique: true},name: Type.String })\n\n");
        
        HelpUtil.appendMethods(builder, DatabaseContext.class.getDeclaredMethods());
        return builder.toString();
    }
}
