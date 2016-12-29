package org.daubin.js.database;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class EntityManagerFactoryBuilderTest {
	
    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    @Test
	public void createEntity() throws NoSuchMethodException, SecurityException, SQLException {
		EntityManagerFactoryBuilder builder = EntityManagerFactoryBuilder.newBuilder().
				createSchema().
				databaseSettings(DB_DRIVER, DB_CONNECTION, DB_USER, DB_PASSWORD);

		Class<?> accounts =  
				builder.registerEntity("accounts", ImmutableMap.<String, Object>of("id", ColumnType.INTEGER, "name", ColumnType.STRING));
		EntityManagerFactory entityManagerFactory = builder.
			registerEntity(Account.class).
			build("db");
		
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
		Assert.assertEquals(2, entities.size());
		
		entityManager.getTransaction().begin();
		Account newAccount = new Account().setName("test");
		entityManager.persist(newAccount);
		entityManager.getTransaction().commit();
		
		Object found = entityManager.find(accounts, newAccount.id);
		Assert.assertEquals("accounts{id:" + newAccount.id + ",name:test}", found.toString());
	}
    
    @Test
	public void script() throws NoSuchMethodException, SecurityException, SQLException, ScriptException, IOException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

        try (Reader reader = new InputStreamReader(
        		EntityManagerFactoryBuilderTest.class.getResourceAsStream(
        				"/" + EntityManagerFactoryBuilderTest.class.getPackage().getName().replace('.', '/') + "/test.js"))) {
        	Object returnValue = engine.eval(reader);
			Assert.assertTrue(returnValue instanceof Number);
			Assert.assertTrue(((Number)returnValue).intValue() > 0);
        }
        
        System.err.println(((Invocable)engine).invokeFunction("help"));
	}
	
	
    @Entity
    @Table(name = "accounts")
    public static class Account {
    	
    	@Id
    	@GeneratedValue
		@Column(unique=true)
    	Integer id;
    	
    	@Column
    	String name;
    	
    	public Account() {
    	}
    	
    	public Account(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public Account setId(int id) {
			this.id = id;
			return this;
		}

		public Account setName(String name) {
			this.name = name;
			return this;
		}
    }
}
