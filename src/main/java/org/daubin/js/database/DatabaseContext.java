package org.daubin.js.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.daubin.js.database.Columns.ColumnAndType;

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

    public Object define(String tableName, Map<String,Object> map) throws SQLException {
        
        List<ColumnAndType> columns = Columns.createColumns(map);
        
        Class<?> clazz = classGenerator.createClass(tableName, columns);
        
        Dao<?, ?> dao = DaoManager.createDao(connection, clazz);
        return new JsDao(dao, clazz);
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
