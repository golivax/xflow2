/* 
 * 
 * XFlow
 * _______
 * 
 *  
 *  (C) Copyright 2010, by Universidade Federal do Pará (UFPA), Francisco Santana, Jean Costa, Pedro Treccani and Cleidson de Souza.
 * 
 *  This file is part of XFlow.
 *
 *  XFlow is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  XFlow is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XFlow.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  ============
 *  BaseDAO.java
 *  ============
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.entity.dao;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import br.usp.ime.lapessc.xflow2.entity.database.EntityManagerHelper;
import br.usp.ime.lapessc.xflow2.exception.persistence.AccessDeniedException;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.exception.persistence.UnableToReachDatabaseException;

public abstract class BaseDAO<T> {

	protected boolean insert(final T entity) throws DatabaseException {
		final EntityManager manager = EntityManagerHelper.getEntityManager();

		try {
			manager.getTransaction().begin();
			manager.persist(entity);
			manager.getTransaction().commit();
		} catch (javax.persistence.PersistenceException e){
			e.printStackTrace();
			if(e.getCause().getCause().getMessage().equalsIgnoreCase("Unknown database")){
				e.printStackTrace();
				throw new UnableToReachDatabaseException(e.getCause().getCause().getMessage());
			}
			
			if(e.getCause().getCause().getMessage().contains("Access denied for user")){
				throw new AccessDeniedException("Database error");
			}

			return false;
		}
		return true;
	}

	protected boolean remove(final T entity) throws DatabaseException {
		final EntityManager manager = EntityManagerHelper.getEntityManager();

		try {
			manager.getTransaction().begin();
			manager.remove(entity);
			manager.getTransaction().commit();
		} catch (javax.persistence.PersistenceException e){
			e.printStackTrace();
			if(e.getCause().getMessage().equalsIgnoreCase("Unknown database")){
				throw new UnableToReachDatabaseException(e.getCause().getMessage());
			}
			
			if(e.getCause().getMessage().contains("Access denied for user")){
				throw new AccessDeniedException(e.getCause().getMessage());
			}

			return false;
		}

		return true;
	}

	protected boolean update(final T entity) throws DatabaseException {
		final EntityManager manager = EntityManagerHelper.getEntityManager();

		try {
			manager.getTransaction().begin();
			manager.merge(entity);
			manager.getTransaction().commit();
		} catch (javax.persistence.PersistenceException e){
			if(e.getCause().getCause().getMessage().equalsIgnoreCase("Unknown database")){
				throw new UnableToReachDatabaseException(e.getCause().getCause().getMessage());
			}
			
			if(e.getCause().getCause().getMessage().contains("Access denied for user")){
				throw new AccessDeniedException(e.getCause().getCause().getMessage());
			}

			return false;
		}
		return true;
	}

	protected T findById(final Class<T> clazz, final long id) throws DatabaseException {
		final EntityManager manager = EntityManagerHelper.getEntityManager();
		try {
			return manager.find(clazz, id);
		} catch (NoResultException e){
			return null;
		} catch (javax.persistence.PersistenceException e){
			if(e.getCause().getCause().getMessage().equalsIgnoreCase("Unknown database")){
				throw new UnableToReachDatabaseException(e.getCause().getCause().getMessage());
			}
			
			if(e.getCause().getCause().getMessage().contains("Access denied for user")){
				throw new AccessDeniedException(e.getCause().getCause().getMessage());
			}
			
			return null;
		}
	}
	
	protected T findUnique(final Class<T> clazz, final String query, final Object[] ... parameters) throws DatabaseException {
		final EntityManager manager = EntityManagerHelper.getEntityManager();
		
		final Query q = manager.createQuery(query);
		for (Object[] parameter : parameters) {
			q.setParameter(parameter[0].toString(), parameter[1]);
		}
		try {
			return (T) q.getSingleResult();
		} catch (NoResultException e){
			return null;
		} catch (javax.persistence.PersistenceException e){
			e.printStackTrace();
			if(e.getCause().getCause().getMessage().equalsIgnoreCase("Unknown database")){
				throw new UnableToReachDatabaseException(e.getCause().getCause().getMessage());
			}
			
			if(e.getCause().getCause().getMessage().contains("Access denied for user")){
				throw new AccessDeniedException(e.getCause().getCause().getMessage());
			}
		}
		return null;
	}
	
	protected Object findUniqueObject(final String query, final Object[] ... parameters) throws DatabaseException {
		final EntityManager manager = EntityManagerHelper.getEntityManager();
		
		final Query q = manager.createQuery(query);
		for (Object[] parameter : parameters) {
			q.setParameter(parameter[0].toString(), parameter[1]);
		}
		try {
			return q.getSingleResult();
		} catch (NoResultException e){
			return null;
		} catch (javax.persistence.PersistenceException e){
			e.printStackTrace();
			if(e.getCause().getCause().getMessage().equalsIgnoreCase("Unknown database")){
				throw new UnableToReachDatabaseException(e.getCause().getCause().getMessage());
			}
			
			if(e.getCause().getCause().getMessage().contains("Access denied for user")){
				throw new AccessDeniedException(e.getCause().getCause().getMessage());
			}
		}
		return null;
	}
	
	protected Collection<T> findByQuery(final Class<T> clazz, final String query, final Object[] ... parameters) throws DatabaseException {
		final EntityManager manager = EntityManagerHelper.getEntityManager();
		
		final Query q = manager.createQuery(query);
		for (Object[] parameter : parameters) {
			q.setParameter(parameter[0].toString(), parameter[1]);
		}
		
		try {
			Collection<T> queryResults = q.getResultList();
			return queryResults;
		} catch (NoResultException e){
			return null;
		} catch (javax.persistence.PersistenceException e){
			e.printStackTrace();
			if(e.getCause().getCause().getMessage().equalsIgnoreCase("Unknown database")){
				throw new UnableToReachDatabaseException(e.getCause().getCause().getMessage());
			}
			
			if(e.getCause().getCause().getMessage().contains("Access denied for user")){
				throw new AccessDeniedException(e.getCause().getCause().getMessage());
			}
			return null;
		}
		
	}
	
	protected Collection<?> findObjectsByQuery(final String query, final Object[] ... parameters) throws DatabaseException {
		final EntityManager manager = EntityManagerHelper.getEntityManager();
		
		final Query q = manager.createQuery(query);
		for (Object[] parameter : parameters) {
			q.setParameter(parameter[0].toString(), parameter[1]);
		}
		
		try {
			Collection<?> queryResults = q.getResultList();
			return queryResults;
		} catch (NoResultException e){
			return null;
		} catch (javax.persistence.PersistenceException e){
			if(e.getCause().getCause().getMessage().equalsIgnoreCase("Unknown database")){
				throw new UnableToReachDatabaseException(e.getCause().getCause().getMessage());
			}
			
			if(e.getCause().getCause().getMessage().contains("Access denied for user")){
				throw new AccessDeniedException(e.getCause().getCause().getMessage());
			}
			return null;
		}
		
	}


	protected Collection<T> findAll(final Class<? extends T> myClass) throws DatabaseException {
		final EntityManager manager = EntityManagerHelper.getEntityManager();
		
		try{
			final StringBuilder builder = new StringBuilder("select a from ");
			builder.append(myClass.getName());
			builder.append(" a");
			Collection<T> all = new ArrayList<T>(manager.createQuery(builder.toString()).getResultList());
			return all;
		}catch (NoResultException e) {
			return null;
		} catch (javax.persistence.PersistenceException e){
			if(e.getCause().getCause().getMessage().equalsIgnoreCase("Unknown database")){
				throw new UnableToReachDatabaseException(e.getCause().getCause().getMessage());
			}
			
			if(e.getCause().getCause().getMessage().contains("Access denied for user")){
				throw new AccessDeniedException(e.getCause().getCause().getMessage());
			}
			
			return null;
		}
	}

	
	protected long getLongValueByQuery(final String query, final Object[] ... parameters) throws DatabaseException {
		final EntityManager manager = EntityManagerHelper.getEntityManager();
		
		final Query q = manager.createQuery(query);
		for (Object[] parameter : parameters) {
			q.setParameter(parameter[0].toString(), parameter[1]);
		}
		try {
			final Long queryResult = (Long) q.getSingleResult();
			return Long.parseLong(queryResult.toString());
		} catch (NoResultException e){
			return 0;
		} catch (javax.persistence.PersistenceException e){
			if(e.getCause().getCause().getMessage().equalsIgnoreCase("Unknown database")){
				throw new UnableToReachDatabaseException(e.getCause().getCause().getMessage());
			}
			
			if(e.getCause().getCause().getMessage().contains("Access denied for user")){
				throw new AccessDeniedException(e.getCause().getCause().getMessage());
			}
			
			return -1;
		} catch (NullPointerException e){
			return -1;
		}
	}
	
	
	protected int getIntegerValueByQuery(final String query, final Object[] ... parameters) throws DatabaseException {
		final EntityManager manager = EntityManagerHelper.getEntityManager();
		
		final Query q = manager.createQuery(query);
		for (Object[] parameter : parameters) {
			q.setParameter(parameter[0].toString(), parameter[1]);
		}
		try {
			final Integer queryResult = (Integer) q.getSingleResult();
			return Integer.parseInt(queryResult.toString());
		} catch (NoResultException e){
			return 0;
		} catch (PersistenceException e){
			if(e.getCause().getCause().getMessage().equalsIgnoreCase("Unknown database")){
				throw new UnableToReachDatabaseException(e.getCause().getCause().getMessage());
			}
			
			if(e.getCause().getCause().getMessage().contains("Access denied for user")){
				throw new AccessDeniedException(e.getCause().getCause().getMessage());
			}
			
			return -1;
		} catch (NullPointerException e){
			return -1;
		} catch (ClassCastException e){
			Long queryResult = (Long) q.getSingleResult();
			int result = (int) Long.parseLong(queryResult.toString());
			return result;
		}
	}
	
	
	protected double getDoubleValueByQuery(final String query, final Object[] ... parameters) throws DatabaseException {
		final EntityManager manager = EntityManagerHelper.getEntityManager();
		
		final Query q = manager.createQuery(query);
		for (Object[] parameter : parameters) {
			q.setParameter(parameter[0].toString(), parameter[1]);
		}
		try {
			final Double queryResult = (Double) q.getSingleResult();
			return Double.parseDouble(queryResult.toString());
		} catch (NoResultException e){
			return 0;
		} catch (javax.persistence.PersistenceException e){
			if(e.getCause().getCause().getMessage().equalsIgnoreCase("Unknown database")){
				throw new UnableToReachDatabaseException(e.getCause().getCause().getMessage());
			}
			
			if(e.getCause().getCause().getMessage().contains("Access denied for user")){
				throw new AccessDeniedException(e.getCause().getCause().getMessage());
			}
			
			return -1;
		} catch (NullPointerException e){
			return -1;
		}
	}
}
