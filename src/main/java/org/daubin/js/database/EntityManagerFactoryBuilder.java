package org.daubin.js.database;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.persistence.Column;
import javax.persistence.EntityManagerFactory;

import org.daubin.js.database.Columns.ColumnMetadata;
import org.daubin.js.database.jpa.EntityManagerExtension;
import org.daubin.js.database.jpa.EntityManagerFactoryExtension;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EntityManagerFactoryBuilder {
    
    private final ClassGenerator classGenerator;
    private final List<Class<?>> classes = Lists.newArrayList();
    private final Map<String, Object> properties;
    
    private EntityManagerFactoryBuilder() throws NoSuchMethodException, SecurityException {
        this.classGenerator = new ClassGenerator();
        this.properties = Maps.newHashMap(
        		ImmutableMap.<String,String>of("openjpa.RuntimeUnenhancedClasses", "supported"));
    }
    
    public static EntityManagerFactoryBuilder newBuilder() throws NoSuchMethodException, SecurityException {
    	return new EntityManagerFactoryBuilder();
    }
    
    public EntityManagerFactoryBuilder merge(Map<String, Object> properties) {
    	this.properties.putAll(properties);
    	return this;
    }
    
    public EntityManagerFactoryBuilder createSchema() {
    	return merge(ImmutableMap.<String, Object>of(
				"openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)"));
    }
    
    public EntityManagerFactoryBuilder databaseSettings(String driverName, String url, String userName, String password) {
    	properties.put("openjpa.ConnectionDriverName", driverName);
    	properties.put("openjpa.ConnectionURL", url);
    	properties.put("openjpa.ConnectionUserName", userName);
    	properties.put("openjpa.ConnectionPassword", password);
    	
    	return this;
    }

    /**
     * Creates a new DAO for the given table.  The map should be a map of column names to either
     * a {@link ColumnType} or another map of settings that can populate a {@link Column} instance.
     * 
     * Under the covers, this method generates a class to represent the table model with javax.persistence
     * annotations on the class and its fields.  The generated class is then fed into a Java ORM implementation.
     */
    public Class<?> registerEntity(String tableName, Map<String,Object> map) throws SQLException {
        
        List<ColumnMetadata> columns = Columns.createColumns(map);
        
        Class<?> clazz = classGenerator.createClass(tableName, columns);
        registerEntity(clazz);
        
        return clazz;
    }
    
	public EntityManagerFactoryBuilder registerEntity(Class<?> clazz) {
		classes.add(clazz);
		return this;
	}
	
	public Supplier<EntityManagerExtension> buildThreadLocal(String persistenceUnitName) throws NoSuchMethodException, SecurityException {
		final EntityManagerFactoryExtension factory = build(persistenceUnitName);
		
		final ThreadLocal<EntityManagerExtension> threadLocal = ThreadLocal.withInitial( 
				factory::createEntityManager);
		return threadLocal::get;
	}
    
    public EntityManagerFactoryExtension build(String persistenceUnitName) throws NoSuchMethodException, SecurityException {
    	
    	Map<String, Object> props = Maps.newHashMap(properties);
    	props.put("openjpa.MetaDataFactory", "jpa(Types=" +
    			Joiner.on(';').join(classes.stream().map(Class::getName).iterator()) +
    			")");
    	
    	props.put("openjpa.ClassResolver", classGenerator.getClassResolver());
    	
    	EntityManagerFactory entityManagerFactory = javax.persistence.Persistence.createEntityManagerFactory(
    		    persistenceUnitName, props);
    	return new EntityManagerFactoryExtension(entityManagerFactory);
    }

    public static String help() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Creation:\n");
        builder.append("\tvar context = new DBContext(conn)\n\n");
        
        builder.append("Usage:\n");
        builder.append("\tvar Account = context.define('accounts', {id: {type: Type.Integer, unique: true},name: Type.String })\n\n");
        
        HelpUtil.appendMethods(builder, EntityManagerFactoryBuilder.class.getDeclaredMethods());
        return builder.toString();
    }
}
