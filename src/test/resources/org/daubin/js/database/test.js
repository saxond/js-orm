org.daubin.js.database.Initializer.init()

var builder = EMBuilder.newBuilder().
				createSchema().
				databaseSettings("org.h2.Driver",
    				 "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "")

var Account =  
    builder.registerEntity("accounts", {"id" : Type.INTEGER, "name": Type.STRING});
var entityManagerFactory = builder.build("db");

var em = entityManagerFactory.createEntityManager();

var account = Account.newInstance();
account.name = "test";

em.persist(account)

print(account)

account.id