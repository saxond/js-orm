# js-orm
An ORM for Java 8's Nashorn Javascript Engine

Description
======

js-orm is a lightweight ORM for use in Java 8's javascript console (jjs).
This project is currently just a playground for ideas.

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