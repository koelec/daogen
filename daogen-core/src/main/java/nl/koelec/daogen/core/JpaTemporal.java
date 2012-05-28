/**
 * Copyright
 * ============================================================
 * Project: HVKL-H1
 *
 * $Revision$
 * $Author$
 * $Date$
 * 
 * Environment: JEE 5
 *
 * Copyright ProRail BV 2008
 * ============================================================ 
 */
package nl.koelec.daogen.core;

import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.persistence.TemporalType;

/**
 * Kan gebruikt worden om een Temporal parameter en het bijbehorende {@link TemporalType} te definieren.
 * <p>
 * voorbeeld:
 * 
 * <pre>
 * &lt;code&gt;public interface TreinQuery
 * { 
 *  &lt;code&gt;@&lt;/code&gt;JpaQuery(query=&quot;select t from Trein where t.datum=:datum&quot;)
 *   List&lt;Trein&gt; getTreinBijDatum (
 *    &lt;code&gt;@&lt;/code&gt;JpaParamName(&quot;datum&quot;) &lt;code&gt;
 *    @&lt;/code&gt;JpaTemporal(TemporalType.DATE) Date datum);
 * } 
 * &lt;/code&gt;
 * </pre>
 */
@Target( { PARAMETER
})
@Retention(RUNTIME)
public @interface JpaTemporal {
    /**
     * De TemporalType waarde die bij converteren van de parameter moet worden gebruikt.
     */
    TemporalType value();
}
