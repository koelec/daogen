package nl.koelec.daogen.sample;

import java.util.List;
import nl.koelec.daogen.core.DaoType;
import nl.koelec.daogen.core.JpaNamedQuery;

@DaoType
public interface CustomerDao {
    
    @JpaNamedQuery(query = "select c from Customer c where name = :name")
    List<Customer> findByName(String name);

    @JpaNamedQuery(query = "select c from Customer c where name = :name", throwException = false)
    Customer findByUniqueName(String name);

    @JpaNamedQuery(query = "select c from Customer c where name = :name", throwException = true)
    Customer findByUniqueNameMandatory(String name);
    
    @JpaNamedQuery(query = "select o from Order o where o.customer.id = :id")
    List<Order> findOrdersByCustomerId(Long id);    
    
}
