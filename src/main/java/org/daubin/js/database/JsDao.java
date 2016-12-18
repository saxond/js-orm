package org.daubin.js.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.j256.ormlite.dao.Dao;

public class JsDao {

    private final Dao<?, ?> dao;
    private final Class<?> modelClass;

    public JsDao(Dao<?, ?> dao, Class<?> clazz) {
        this.dao = dao;
        this.modelClass = clazz;
    }

    public List<?> all() throws SQLException {
        return dao.queryForAll();
    }

    public List<?> all(Map<String,Object> fieldValues) throws SQLException {
        return dao.queryForFieldValues(fieldValues);
    }
    
    public Object one(Map<String,Object> fieldValues) throws SQLException {
        List<?> list = dao.queryForFieldValues(fieldValues);
        return list.isEmpty() ? null : list.get(0);
    }
    
    public String help() {
        StringBuilder builder = new StringBuilder();
        
        HelpUtil.appendFields(builder, modelClass.getDeclaredFields()).append('\n');
        
        HelpUtil.appendMethods(builder, JsDao.class.getDeclaredMethods());
        
        return builder.toString();
    }
}
