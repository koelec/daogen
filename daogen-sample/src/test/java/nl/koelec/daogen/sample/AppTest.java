package nl.koelec.daogen.sample;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    private EntityManager em;
    
    @Before
    public void setup() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("testPU");
        em = emf.createEntityManager();    
    }
    
    @After
    public void teardown() {
        em.close();
    }
    
    @Test
    public void test() {
        Customer customer0 = new Customer();
        customer0.setId(1);
        customer0.setName("Joe Smith");
        
        em.getTransaction().begin();

        // Persist the customer
        em.persist(customer0);

        // Create 2 orders
        Order order1 = new Order();
        order1.setId(100);
        order1.setAddress("123 Main St. Anytown, USA");
        order1.setCustomer(customer0);
        customer0.getOrders().add(order1);
        em.persist(order1);
        Order order2 = new Order();
        order2.setId(200);
        order2.setAddress("567 1st St. Random City, USA");
        order2.setCustomer(customer0);
        customer0.getOrders().add(order2);
        em.persist(order2);
        em.flush();
        
        DaoFactory factory = new DaoFactory();
        factory.setEntityManager(em);
        CustomerDao dao = factory.getCustomerDao();
        List<Customer> result = dao.findByName("Joe Smith");
        
        Assert.assertEquals(1, result.size());
        
        List<Order> orders = dao.findOrdersByCustomerId(result.get(0).getId());
        Assert.assertEquals(2, orders.size());
        em.getTransaction().rollback();
        
    }
    
}
