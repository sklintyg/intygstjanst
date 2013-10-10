package se.inera.certificate.migration.job;

import javax.sql.DataSource;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.springtestdbunit.DbUnitRule;

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractDataLoadingSpringTest {

    @Rule
    public DbUnitRule dbUnit = new DbUnitRule();
    
    @Autowired
    @Qualifier("intygDataSource")
    private DataSource dataSource;
    
}
