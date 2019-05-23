package com.example.click;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import java.util.List;

// Inspired by this tutorial:
// https://www.sitepoint.com/hibernate-introduction-persisting-java-objects/
public class ClickDao {
    private EntityManager entityManager;
    private EntityTransaction entityTransaction;

    public ClickDao( EntityManager entityManager ) {
        this.entityManager = entityManager;
        this.entityTransaction = this.entityManager.getTransaction();
    }

    public ClickDao() {
        this( Persistence.createEntityManagerFactory("click-unit").createEntityManager() );
    }

    public Click persist( String ip ) {
        Click click = findByIp( ip );

        if ( click != null ) {
            update( click.getId(), click.getCount() + 1 );
        }
        else {
            click = create( ip );
        }

        return click;
    }

    public Click findByIp( String ip ) {
        final TypedQuery<Click> query = entityManager.createQuery( "FROM Click WHERE ip = :ip", Click.class );

        query.setParameter( "ip", ip );

        try {
            return query.getSingleResult();
        }
        catch ( NoResultException e ) {
            return null;
        }
    }

    public Click create( String ip ) {
        final Click click = new Click().setIp( ip );

        beginTransaction();

        entityManager.persist( click );

        commitTransaction();

        return click;
    }

    public Click update( long id, long counter ) {
        beginTransaction();

        Click click = entityManager.find( Click.class, id );

        // let a NPE get thrown..

        click.setCount( counter );

        entityManager.merge( click );

        commitTransaction();

        return click;
    }

    public Click remove( int id ) {
        beginTransaction();

        Click click = entityManager.find( Click.class, id );

        entityManager.remove( click );

        commitTransaction();

        return click;
    }

    public Click find( int id ) {
        return entityManager.find( Click.class, id );
    }

    public List<Click> findAll() {
        final TypedQuery<Click> query = entityManager.createQuery("FROM Click", Click.class);

        return query.getResultList();
    }

    private void beginTransaction() {
        try {
            entityTransaction.begin();
        }
        catch ( IllegalStateException e ) {
            rollbackTransaction();
        }
    }

    private void commitTransaction() {
        try {
            entityTransaction.commit();

            entityManager.close();
        }
        catch ( IllegalStateException e ) {
            rollbackTransaction();
        }
    }

    private void rollbackTransaction() {
        try {
            entityTransaction.rollback();
        }
        catch ( IllegalStateException e ) {
            e.printStackTrace();
        }
    }
}