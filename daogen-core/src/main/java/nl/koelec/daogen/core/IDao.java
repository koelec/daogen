/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.koelec.daogen.core;

/**
 *
 * @author Chris
 */
public interface IDao {
    <T> T persist(T entity);
    <T> T merge(T entity);
    void refresh(Object entity);
    void remove(Object entity);
}
