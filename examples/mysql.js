//jjs -cp orm/build/libs/js-orm-0.6.66-SNAPSHOT.jar orm/examples/mysql.js

org.daubin.js.database.init()

var conn = new ConnectionSource("jdbc:mysql://localhost/test", "root", null)
var context = new DBContext(conn)
var Account = context.define(
                  'accounts',
                  {id: {type: Type.Integer, unique: true},
                  name: Type.String })