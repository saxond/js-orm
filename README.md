# js-orm
An ORM for Java 8's Nashorn Javascript Engine

Description
======

js-orm is a lightweight ORM for use in Java 8's javascript console (jjs).
The goal is to provide simple database support for Nashorn scripts with
a performant java implementation.  This project is currently just a playground for ideas.

The javascript syntax is very loosely based on [sequelize](http://docs.sequelizejs.com/en/v3/)
and the implementation is currently built on top of Open JPA. 

Why do this?
=====

I want a simple javscript ORM with a solid Java implementation.  One could 
reasonably argue that the network overhead of database operations is much greater than the 
overhead of a pure JS solution, so the optimizations of an implementation like this don't 
really move the dial.  Fine point.  Regardless, this seemed like a fun thing to do.  I like 
the idea of generating a Java class using a terse associative array of metadata.    

Build
======

    gradle jar
    
Run
=======

    jjs -cp orm/build/libs/js-orm-0.6.66-SNAPSHOT.jar
    
    
Example
=====
    jjs> var builder = EMBuilder.newBuilder().createSchema().databaseSettings("org.h2.Driver","jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "")

    jjs> var Account = builder.registerEntity("accounts", {"id" : {type: Type.INTEGER, id: true, generatedValue: true}, "name": Type.STRING});
    jjs> var entityManagerFactory = builder.build("db");

    jjs> var em = entityManagerFactory.createEntityManager();