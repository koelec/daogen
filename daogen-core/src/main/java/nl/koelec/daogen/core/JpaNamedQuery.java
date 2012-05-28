package nl.koelec.daogen.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.persistence.QueryHint;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JpaNamedQuery {

    /**
     * De query string in de Java Persistence query taal.
     */
    String query();

    /**
     * Vendor specifieke query hints.
     */
    QueryHint[] hints() default {};

    /**
     * Only valid for queries that return a single result. <br> When set to
     * false (default) the NoResultException is catched internally and
     * <code>null</code> is returned.
     */
    boolean throwException() default false;
}
