import oracle.jdbc.pool.OracleDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/*

Oracle database used: 
Downloaded from http://www.oracle.com/technetwork/database/enterprise-edition/databaseappdev-vm-161299.html
Oracle DB Developer VM (7,227,840,000 bytes, md5sum: 6829d0a691010663ddb397c07fcf8150)

Transfer this file to the VM.

Compile and run the code:
$ javac -cp /u01/oracle/app/oracle/product/12.1.0/dbhome_1/jdbc/lib/ojdbc7.jar Bug.java && java -cp /u01/oracle/app/oracle/product/12.1.0/dbhome_1/jdbc/lib/ojdbc7.jar:. Bug

*/
public class Bug {

    public static void main(String[] args) throws Exception {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setURL("jdbc:oracle:thin:@localhost:1521/orcl");
        dataSource.setUser("sys as sysdba");
        dataSource.setPassword("oracle");

        try {
            setupQueue(dataSource);
            runIt(dataSource);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
        }

    }

    private static void runIt(DataSource dataSource) throws Exception {
        emptyQueue(dataSource);
        System.err.println("queue is empty");

        AtomicInteger counter = new AtomicInteger(0);
        while (true) {
            try (Connection conn = getConnection(dataSource)) {
                for (int j = 0; j < 1000; j++) {
                    System.err.println(">>> " + counter.incrementAndGet());
                    if (Math.random() > 0.5) {
                        emptyQueueOnConn(conn);
                    }
                    for (int i = 0; i < 100; i++) {
                        enqueSingle(conn);
                    }
                    for (int i = 0; i < 25; i++) {
                        dequeSingle(conn);
                    }
                    if (Math.random() > 0.5) {
                        conn.rollback();
                    } else {
                        conn.commit();
                    }
                }
            }
        }
    }

    private static void emptyQueue(DataSource dataSource) throws SQLException {
        try (Connection conn = getConnection(dataSource)) {
            emptyQueueOnConn(conn);
        }
    }

    private static Connection getConnection(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        return connection;
    }

    private static void emptyQueueOnConn(Connection conn) throws SQLException {
        while (dequeSingle(conn) != null) {
        }
        conn.commit();
    }

    private static String dequeSingle(Connection conn) throws SQLException {
        try (CallableStatement cs = conn.prepareCall(
                "DECLARE " +
                        "   dequeue_options     dbms_aq.dequeue_options_t;" +
                        "   message_properties  dbms_aq.message_properties_t;" +
                        "   message_handle      RAW(16);" +
                        "   aq_message          MY_Q_T;" +
                        "BEGIN " +
                        "   dequeue_options.wait := 0;" +
                        // Uncommenting the following line will postpone (but not fix) the error.
                        // "   dequeue_options.navigation := DBMS_AQ.FIRST_MESSAGE;" + // Default .navigation is DBMS_AQ.NEXT_MESSAGE.
                        "   dbms_aq.dequeue(queue_name => 'MY_Q', dequeue_options => dequeue_options, message_properties => message_properties, payload => aq_message, msgid => message_handle);"
                        +
                        "   ? := aq_message.payload; " +
                        "END;")) {
            cs.registerOutParameter(1, Types.VARCHAR);
            try {
                cs.execute();
                return cs.getString(1);
            } catch (SQLException e) {
                if (e.getErrorCode() == 25228) { // AQ TIMEOUT
                    return null;
                } else {
                    throw e;
                }
            }
        }
    }

    private static void enqueSingle(Connection conn) throws SQLException {
        String sql = "DECLARE " +
                "   enqueue_options dbms_aq.enqueue_options_t;" +
                "   message_properties dbms_aq.message_properties_t;" +
                "   message_handle RAW(16);" +
                "   aq_message MY_Q_T;" +
                "BEGIN " +
                "   aq_message := MY_Q_T(?);" +
                "   dbms_aq.enqueue( queue_name => 'MY_Q', enqueue_options => enqueue_options, message_properties => message_properties, payload => aq_message, msgid => message_handle);"
                +
                "END;";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, UUID.randomUUID().toString());
            cs.execute();
        }
    }

    // ****************
    // Start setup code
    // ****************
    public static void setupQueue(DataSource dataSource) {
        try (Connection conn = getConnection(dataSource)) {
            if (0 == queryForInt(conn, "select count(*) from user_types where type_name = 'MY_Q_T'")) {
                try (PreparedStatement ps = conn.prepareStatement("CREATE OR REPLACE TYPE MY_Q_T AS OBJECT (payload varchar2(255))")) {
                    ps.execute();
                }
            }

            if (0 == queryForInt(conn, "select count(*) from user_queue_tables where queue_table = 'MY_Q_TABLE'")) {
                try (CallableStatement cs = conn.prepareCall(
                        "call dbms_aqadm.create_queue_table (queue_table => ?, queue_payload_type => ?)")) {
                    cs.setString(1, "MY_Q_TABLE");
                    cs.setString(2, "MY_Q_T");
                    cs.execute();
                }
            }

            if (0 == queryForInt(conn, "select count(*) from user_queues where name = 'MY_Q'")) {
                try (CallableStatement cs = conn.prepareCall("call dbms_aqadm.create_queue (queue_name => ?, queue_table => ?)")) {
                    cs.setString(1, "MY_Q");
                    cs.setString(2, "MY_Q_TABLE");
                    cs.execute();
                }
            }

            try (CallableStatement cs = conn.prepareCall("call dbms_aqadm.start_queue (queue_name => ?)")) {
                cs.setString(1, "MY_Q");
                cs.execute();
            }
            conn.commit();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    private static int queryForInt(Connection conn, String sql) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("No results!");
                }
                int res = rs.getInt(1);
                if (rs.next()) {
                    throw new RuntimeException("More than one row!");
                }
                return res;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
