package org.daubin.js.database.jpa;

import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public interface EntityManagerExtension extends EntityManager {
    default void doWithTransaction(Consumer<EntityTransaction> work) {
    	final EntityTransaction transaction = getTransaction();
    	transaction.begin();
    	
    	work.accept(transaction);
    	
    	transaction.commit();
    }
}
