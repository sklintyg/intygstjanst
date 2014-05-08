package se.inera.certificate.migration.testutils.dbunit;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;

/**
 * Abstract base class for data loading unit tests.
 * 
 * @author nikpet
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
    DbUnitTestExecutionListener.class, TransactionalTestExecutionListener.class})
@DbUnitConfiguration(databaseConnection = "certificateDataSource", dataSetLoader = CustomFlatXmlDataSetLoader.class)
public abstract class AbstractDbUnitSpringTest {

    public AbstractDbUnitSpringTest() {
        
    }

}
