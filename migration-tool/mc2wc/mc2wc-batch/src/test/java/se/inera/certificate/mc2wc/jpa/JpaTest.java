package se.inera.certificate.mc2wc.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import se.inera.certificate.mc2wc.jpa.model.Certificate;
import se.inera.certificate.mc2wc.jpa.model.Question;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/persistance-context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
    DbUnitTestExecutionListener.class, TransactionalTestExecutionListener.class})
@DbUnitConfiguration(databaseConnection = "medcertDataSource")
@DatabaseSetup({"/data/question.xml"})
public class JpaTest {
    
    @PersistenceContext
    private EntityManager em;
    
    @Test
    public void testSomething() {
        
        TypedQuery<Certificate> query = em.createQuery("select c from Certificate c", Certificate.class);
        Certificate cert = query.getSingleResult();
        assertNotNull(cert);
        assertEquals(1, cert.getQuestions().size());
        
    }
    
}
