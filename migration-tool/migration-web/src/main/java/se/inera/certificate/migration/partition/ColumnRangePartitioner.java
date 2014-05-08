package se.inera.certificate.migration.partition;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Partitioner for partitioning a set of database ids into a number of
 * ExecutionContexts.
 * 
 * @author nikpet
 * 
 */
public class ColumnRangePartitioner implements Partitioner {

    private static Logger logger = LoggerFactory.getLogger(ColumnRangePartitioner.class);

    private JdbcOperations jdbcTemplate;

    private String table;

    private String column;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        int minId = jdbcTemplate.queryForInt("SELECT MIN(" + column + ") from " + table);
        logger.debug("Min id in column {} of table {} is {}", new Object[] { column, table, minId });

        int maxId = jdbcTemplate.queryForInt("SELECT MAX(" + column + ") from " + table);
        logger.debug("Max id in column {} of table {} is {}", new Object[] { column, table, maxId });

        // calc desired size of partition
        int partitionSize = (maxId - minId) / gridSize + 1;

        Map<String, ExecutionContext> execContexts = new HashMap<String, ExecutionContext>();

        int partitionNbr = 0;
        int partitionStart = minId;

        int partitionEnd = partitionStart + partitionSize - 1;

        String partitionName = null;
        
        // assign ranges of ids to partitions
        while (partitionStart <= maxId) {

            partitionName = "partition" + partitionNbr;

            ExecutionContext execCtx = new ExecutionContext();
            execContexts.put(partitionName, execCtx);

            if (partitionEnd >= maxId) {
                partitionEnd = maxId;
            }

            execCtx.putInt("minValue", partitionStart);
            execCtx.putInt("maxValue", partitionEnd);

            logger.debug("Created partition {} with start {} and end {}", 
                    new Object[] { partitionName, partitionStart,
                    partitionEnd });

            partitionStart += partitionSize;
            partitionEnd += partitionSize;

            partitionNbr++;
        }

        logger.info("Partitioned the data in column {} in table {} data into {} ExecutionContexts", new Object[] {
                column, table, execContexts.size() });

        return execContexts;
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

}
