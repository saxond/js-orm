package org.daubin.js.database.jpa;

import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.daubin.classutils.ProxyGenerator;

public class EntityManagerFactoryExtension extends DelegatingEntityManagerFactory {

	public EntityManagerFactoryExtension(EntityManagerFactory delegate) {
		super(delegate);
	}

	@Override
	public EntityManagerExtension createEntityManager() {
		try {
			EntityManager em = super.createEntityManager();
			
			Function<EntityManager, EntityManagerExtension> factory = ProxyGenerator.createInterfaceExtensionFactory(EntityManagerExtension.class);
			return factory.apply(em);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
