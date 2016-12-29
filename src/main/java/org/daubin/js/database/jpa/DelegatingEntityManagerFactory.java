package org.daubin.js.database.jpa;

import java.util.Map;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

public class DelegatingEntityManagerFactory implements EntityManagerFactory {
	protected final EntityManagerFactory delegate;

	public DelegatingEntityManagerFactory(EntityManagerFactory delegate) {
		super();
		this.delegate = delegate;
	}

	public EntityManager createEntityManager() {
		return delegate.createEntityManager();
	}

	public EntityManager createEntityManager(Map map) {
		return delegate.createEntityManager(map);
	}

	public CriteriaBuilder getCriteriaBuilder() {
		return delegate.getCriteriaBuilder();
	}

	public Metamodel getMetamodel() {
		return delegate.getMetamodel();
	}

	public boolean isOpen() {
		return delegate.isOpen();
	}

	public void close() {
		delegate.close();
	}

	public Map<String, Object> getProperties() {
		return delegate.getProperties();
	}

	public Cache getCache() {
		return delegate.getCache();
	}

	public PersistenceUnitUtil getPersistenceUnitUtil() {
		return delegate.getPersistenceUnitUtil();
	}
	
	
}
