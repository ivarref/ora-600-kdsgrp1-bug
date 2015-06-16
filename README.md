dbms_aq.deque ORA-600 kdsgrp1 bug
=================================

Oracle database used
--------------------

Downloaded from http://www.oracle.com/technetwork/database/enterprise-edition/databaseappdev-vm-161299.html

Oracle DB Developer VM (7,227,840,000 bytes, md5sum: 6829d0a691010663ddb397c07fcf8150)


How to reproduce bug
--------------------

Do the following inside a terminal in the VM image:
    
    $ (copy/paste the contents of src/main/java/Bug.java into Bug.java)

    $ javac -cp /u01/oracle/app/oracle/product/12.1.0/dbhome_1/jdbc/lib/ojdbc7.jar Bug.java && java -cp /u01/oracle/app/oracle/product/12.1.0/dbhome_1/jdbc/lib/ojdbc7.jar:. Bug

Number of iterations
--------------------

I usually got the error well before 200 iterations.

Sample output
-------------

Sample console output when running Bug.java

    queue is empty
    >>> 1
    >>> 2
    >>> 3
    (...)
    >>> 55
    java.sql.SQLException: ORA-00600: intern feilkode, argumenter: [kdsgrp1], [], [], [], [], [], [], [], [], [], [], []
    ORA-06512: ved "SYS.DBMS_AQ", line 366
    ORA-06512: ved line 1

    	at oracle.jdbc.driver.T4CTTIoer.processError(T4CTTIoer.java:450)
    	at oracle.jdbc.driver.T4CTTIoer.processError(T4CTTIoer.java:399)
    	at oracle.jdbc.driver.T4C8Oall.processError(T4C8Oall.java:1059)
    	at oracle.jdbc.driver.T4CTTIfun.receive(T4CTTIfun.java:522)
    	at oracle.jdbc.driver.T4CTTIfun.doRPC(T4CTTIfun.java:257)
    	at oracle.jdbc.driver.T4C8Oall.doOALL(T4C8Oall.java:587)
    	at oracle.jdbc.driver.T4CCallableStatement.doOall8(T4CCallableStatement.java:220)
    	at oracle.jdbc.driver.T4CCallableStatement.doOall8(T4CCallableStatement.java:48)
    	at oracle.jdbc.driver.T4CCallableStatement.executeForRows(T4CCallableStatement.java:938)
    	at oracle.jdbc.driver.OracleStatement.doExecuteWithTimeout(OracleStatement.java:1150)
    	at oracle.jdbc.driver.OraclePreparedStatement.executeInternal(OraclePreparedStatement.java:4798)
    	at oracle.jdbc.driver.OraclePreparedStatement.execute(OraclePreparedStatement.java:4901)
    	at oracle.jdbc.driver.OracleCallableStatement.execute(OracleCallableStatement.java:5631)
    	at oracle.jdbc.driver.OraclePreparedStatementWrapper.execute(OraclePreparedStatementWrapper.java:1385)
    	at bug.Bug.dequeSingle(Bug.java:93)
    	at bug.Bug.emptyQueueOnConn(Bug.java:71)
    	at bug.Bug.runIt(Bug.java:40)
    	at bug.Bug.main(Bug.java:22)

