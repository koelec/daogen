/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.koelec.daogen.core;

import java.util.Calendar;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;

/**
 *
 * @author Chris
 */
public abstract class AbstractDao implements IDao {

    protected abstract EntityManager getEntitymanager();

    @Override
    public <T> T persist(T entity) {
        getEntitymanager().persist(entity);
        return entity;
    }

    @Override
    public <T> T merge(T entity) {
        return getEntitymanager().merge(entity);
    }

    @Override
    public void refresh(Object entity) {
        getEntitymanager().refresh(entity);
    }

    @Override
    public void remove(Object entity) {
        getEntitymanager().remove(entity);
    }

    protected void setParam(String paramName, Object paramValue, TemporalType temporalType, Query query) {
        if (temporalType != null) {
            if (paramValue instanceof Calendar) {
                query.setParameter(paramName, (Calendar) paramValue, temporalType);
            } else if (paramValue instanceof Date) {
                query.setParameter(paramName, (Date) paramValue, temporalType);
            } else {
                throw new RuntimeException("Temporal type parameter " + paramName
                        + " type must be either java.util.Calendar or java.util.Date");
            }
        } else {
            query.setParameter(paramName, paramValue);
        }
    }
}
