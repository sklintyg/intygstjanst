package se.inera.certificate.mc2wc.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.Test;

import se.inera.certificate.mc2wc.dbunit.AbstractDbUnitSpringTest;
import se.inera.certificate.mc2wc.jpa.model.Certificate;

import com.github.springtestdbunit.annotation.DatabaseSetup;

@DatabaseSetup({"/data/question.xml"})
public class JpaTest extends AbstractDbUnitSpringTest {
    
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
