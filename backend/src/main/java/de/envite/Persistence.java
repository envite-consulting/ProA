package de.envite;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class Persistence {

	@Inject
	EntityManager em;

	@Transactional
	public List<MyEntity> doSomething() {
		MyEntity entity1 = new MyEntity();
		entity1.field = "field-1";
		em.persist(entity1);

		return em.createQuery("from MyEntity", MyEntity.class).getResultList();
	}

}
