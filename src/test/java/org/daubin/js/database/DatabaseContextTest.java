package org.daubin.js.database;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.daubin.js.database.Initializer;
import org.daubin.js.database.JsDao;
import org.junit.Test;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

public class DatabaseContextTest {
    
    @Test
    public void help() throws ScriptException, NoSuchMethodException, SQLException {
        String script = Initializer.class.getName() + ".init();" + 
                "help(DBContext)";
        
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        
        engine.eval(script);
        
        System.err.println(((Invocable)engine).invokeFunction("help"));
    }


    @Test
    public void test() throws ScriptException, NoSuchMethodException, SQLException {
        String script = Initializer.class.getName() + ".init();" + 
                "function test() { var t = new DBContext(conn).define('accounts', " + 
                "{id: {type: Type.Integer, unique: true},name: Type.String });" +
                "return t }";
        
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        
        Bindings bindings = engine.createBindings();
        bindings.put("conn", createConnection());
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        engine.eval(script);
        
        JsDao value = (JsDao) ((Invocable)engine).invokeFunction("test");
        
        List all = value.all();
        System.err.println(all);
    }

    private ConnectionSource createConnection() throws SQLException {
      String serverName = "localhost";
      String mydatabase = "aggregator_test";
      return new JdbcConnectionSource("jdbc:mysql://" + serverName + "/" + mydatabase, "root", null);

    }
}
