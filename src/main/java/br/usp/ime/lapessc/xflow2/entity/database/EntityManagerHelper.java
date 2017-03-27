package br.usp.ime.lapessc.xflow2.entity.database;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class EntityManagerHelper {

    private static final EntityManagerFactory emf;
    private static final ThreadLocal<EntityManager> threadLocal;

    static {
        emf = Persistence.createEntityManagerFactory("xflow-persistence-unit");
        threadLocal = new ThreadLocal<EntityManager>();
    }

    public static EntityManager getEntityManager() throws DatabaseException{
        EntityManager em = threadLocal.get();

        if (em == null) {
            em = emf.createEntityManager();
            // set your flush mode here
            threadLocal.set(em);
        }
        return em;
    }

    public static void closeEntityManager() {
        EntityManager em = threadLocal.get();
        if (em != null) {
            em.close();
            threadLocal.set(null);
        }
    }

    public static void closeEntityManagerFactory() {
        emf.close();
    }

}