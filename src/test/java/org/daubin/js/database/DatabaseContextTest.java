package org.daubin.js.database;

import java.sql.SQLException;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseContextTest {
    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";
    
    @BeforeClass
    public static void beforeClass() throws SQLException {
    	createDb(createConnection());
    }
    
    @Test
    public void help() throws ScriptException, NoSuchMethodException, SQLException {
        String script = Initializer.class.getName() + ".init();" + 
                "help(DBContext)";
        
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        
        engine.eval(script);
        
        System.err.println(((Invocable)engine).invokeFunction("help"));
    }
    
    @Test
    public void test() throws NoSuchMethodException, SecurityException, SQLException {
    	DatabaseContext context = new DatabaseContext(createConnection());
    	
    }


    @Test
    public void testScript() throws ScriptException, NoSuchMethodException, SQLException {
        String script = Initializer.class.getName() + ".init();" + 
                "function test() { var t = new DBContext(conn).define('accounts', " + 
                "{id: {type: Type.INTEGER, unique: true, nullable: false},name: Type.STRING });" +
                "return t }";
        
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        
        initializeEngine(engine);
        engine.eval(script);
        
        JsEntityManager value = (JsEntityManager) ((Invocable)engine).invokeFunction("test");
        
        List<Model> all = value.all();
        System.err.println(all);
        System.err.println(all.get(0).help());
    }

	private void initializeEngine(ScriptEngine engine) throws SQLException {
		Bindings bindings = engine.createBindings();
        bindings.put("conn", createConnection());
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
	}
    
    @Test
    public void badColumn() throws ScriptException, NoSuchMethodException, SQLException {
        String script = Initializer.class.getName() + ".init();" + 
                "function test() { var t = new DBContext(conn).define('accounts', " + 
                "{id: {type: Type.INTEGER, unique: true, nullable: false, dude: true},name: Type.STRING });" +
                "return t }";
        
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        
        initializeEngine(engine);
        engine.eval(script);
        
        try {
        	JsEntityManager value = (JsEntityManager) ((Invocable)engine).invokeFunction("test");
        	Assert.fail();
        } catch (RuntimeException e) {
        	Assert.assertEquals("Column `id` references unsupported keys: [dude]", e.getMessage());
        }
    }

    private static ConnectionSource createConnection() throws SQLException {
      return new JdbcConnectionSource(DB_CONNECTION, DB_USER, DB_PASSWORD);
    }
    
    private static ConnectionSource createDb(ConnectionSource conn) throws SQLException {
    	TableUtils.createTable(conn, Account.class);
    	
    	Dao<Account, ?> dao = DaoManager.createDao(conn, Account.class);
    	dao.create(new Account(5, "test"));
    	
    	return conn;
    }
    
    @Table(name = "accounts")
    private static class Account {
    	
		@Column(unique=true)
    	int id;
    	@Column
    	String name;
    	
    	public Account() {
    	}
    	
    	public Account(int id, String name) {
			this.id = id;
			this.name = name;
		}

    }
}
