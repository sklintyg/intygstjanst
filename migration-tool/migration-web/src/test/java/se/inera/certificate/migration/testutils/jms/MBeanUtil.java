package se.inera.certificate.migration.testutils.jms;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBeanUtil {

    private static final Logger log = LoggerFactory.getLogger(MBeanUtil.class);

    public String amqJmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";

    public MBeanUtil() {
        
    }

    public MBeanServerConnection connect() throws IOException {
        
        JMXConnector connector = null;
        
        MBeanServerConnection connection = null;

        String username = "";
        String password = "";

        Map<String, Object> env = new HashMap<String, Object>();
        
        String[] credentials = new String[] { username, password };
        env.put(JMXConnector.CREDENTIALS, credentials);

        try {
            connector = JMXConnectorFactory.newJMXConnector(new JMXServiceURL(amqJmxUrl), env);
            connector.connect();
            
            connection = connector.getMBeanServerConnection();
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public void count(MBeanServerConnection conn) throws IOException {
        int numberOfMBeans = conn.getMBeanCount().intValue();
        log.info("Number of MBeans currently running: " + numberOfMBeans);
    }

    public void query(MBeanServerConnection conn, String query) throws IOException {
        if (conn != null && query != null) {
            listMBeans(conn, query);
        } else if (conn != null && query.equals("")) {
            listAllMBeanNames(conn);
        } else {
            log.error("Unable to connect to ActiveMQ");
        }
    }

    public void listMBeans(MBeanServerConnection conn, String query) throws IOException {
        ObjectName name;
        Set names = null;
        try {
            name = new ObjectName(query);
            names = conn.queryMBeans(name, name);
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        for (Iterator iter = names.iterator(); iter.hasNext();) {
            ObjectInstance obj = (ObjectInstance) iter.next();
            log.info("+ " + obj.getClassName());
        }
    }

    public void listAllMBeanNames(MBeanServerConnection conn) throws IOException {
        Set names = getAllMBeanNames(conn);

        for (Iterator iter = names.iterator(); iter.hasNext();) {
            ObjectName objName = (ObjectName) iter.next();
            log.info("+ " + objName);
        }
    }

    public void listMBeanAttrs(MBeanServerConnection conn, String query) throws IOException {
        ObjectName objName = null;
        try {
            objName = new ObjectName(query);
            log.info("+ " + objName.getCanonicalName());

            MBeanInfo info = getMBeanInfo(conn, objName);
            MBeanAttributeInfo[] attrs = info.getAttributes();

            for (int i = 0; i < attrs.length; ++i) {
                Object obj = conn.getAttribute(objName, attrs[i].getName());
                log.info("  - " + attrs[i].getName() + obj);
            }
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        }
    }

    private MBeanInfo getMBeanInfo(MBeanServerConnection conn, ObjectName objName) throws IOException {
        MBeanInfo info = null;

        try {
            info = conn.getMBeanInfo((ObjectName) objName);
        } catch (InstanceNotFoundException e1) {
            e1.printStackTrace();
        } catch (IntrospectionException e1) {
            e1.printStackTrace();
        } catch (ReflectionException e1) {
            e1.printStackTrace();
        }

        return info;
    }

    private Set getAllMBeanNames(MBeanServerConnection conn) throws IOException {
        return conn.queryNames(null, null);
    }
}
