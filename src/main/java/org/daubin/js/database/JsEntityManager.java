package org.daubin.js.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.j256.ormlite.dao.Dao;

public class JsEntityManager {

    private final Dao<Model, ?> dao;
    private final Class<?> modelClass;

    public JsEntityManager(Dao<Model, ?> dao, Class<?> clazz) {
        this.dao = dao;
        this.modelClass = clazz;
    }
    
    //---  Query operations ---//

    public List<Model> all() throws SQLException {
        return dao.queryForAll();
    }

    public List<Model> all(Map<String,Object> fieldValues) throws SQLException {
        return dao.queryForFieldValues(fieldValues);
    }
    
    public Model one(Map<String,Object> fieldValues) throws SQLException {
        List<Model> list = dao.queryForFieldValues(fieldValues);
        return list.isEmpty() ? null : list.get(0);
    }
    
    public void persist(Model data) throws SQLException {
    	dao.create(data);
    }
    
    public String help() {
        StringBuilder builder = new StringBuilder();
        
        HelpUtil.appendFields(builder, modelClass.getDeclaredFields()).append('\n');
        
        HelpUtil.appendMethods(builder, JsEntityManager.class.getDeclaredMethods());
        
        return builder.toString();
    }
}
