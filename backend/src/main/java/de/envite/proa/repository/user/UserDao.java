package de.envite.proa.repository.user;

import de.envite.proa.repository.tables.UserTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class UserDao {

    @Inject
    EntityManager em;

    @Transactional
    public UserTable findByEmail(String email) {
        try {
            return em.createQuery("SELECT u FROM UserTable u WHERE u.email = :email", UserTable.class)
                    .setParameter("email", email).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public UserTable findById(Long id) {
        return em.find(UserTable.class, id);
    }

    @Transactional
    public UserTable patchUser(UserTable user) {
        return em.merge(user);
    }

    @Transactional
    public List<UserTable> getAllUsers() {
        return em.createQuery("SELECT u FROM UserTable u", UserTable.class).getResultList();
    }

    @Transactional
    public void deleteById(Long id) {
		UserTable user = findById(id);
		em.remove(user);
    }
}
