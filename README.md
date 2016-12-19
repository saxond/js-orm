# js-orm
An ORM for Java 8's Nashorn Javascript Engine

Description
======

js-orm is a lightweight ORM for use in Java 8's javascript console (jjs).
The goal is to provide simple database support for Nashorn scripts with
a performant java implementation.  This project is currently just a playground for ideas.

The javascript syntax is very loosely based on [sequelize](http://docs.sequelizejs.com/en/v3/)
and the implementation is currently built on top of [ORMLite](http://ormlite.com). 

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
    jjs> org.daubin.js.database.Initializer.init()
    jjs> var conn = new ConnectionSource("jdbc:mysql://localhost/test", "root", null)
    jjs> var context = new DBContext(conn)
    jjs> var Account = context.define('accounts',{id: {type: Type.Integer, unique: true},name: Type.String })
    jjs> Account.all()