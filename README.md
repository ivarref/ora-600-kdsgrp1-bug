# dbms_aq.deque ORA-600 6033 / kdsgrp1 / kdifxs0: no key bug

## History

### 2015-06-29
This report has led to Oracle filing Bug 21302755, so development is looking at this issue.

### 2015-10-21
So this bug has showcased that it is possible to crash the Oracle Database using a single
connection in a single thread using the most basic AQ features.
This makes, in my opinion, Oracle AQ quite flawed.

Oracle Support has stated that this bug is being worked on.
However, if I was to choose a Oracle-based database queue today, I would implement my own using
`FOR UPDATE SKIP LOCKED` and not use Oracle AQ.

### 2016-02-03
Bug still exist.

## Oracle database used

Downloaded from http://www.oracle.com/technetwork/database/enterprise-edition/databaseappdev-vm-161299.html

Oracle DB Developer VM (7,396,868,608 bytes, md5sum: dda3d26031040b2fbff6d4b5e7c67081)

## How to reproduce bug

Do the following inside a terminal in the VM image:
    
    $ (copy/paste the contents of src/main/java/Bug.java into Bug.java)

    or:

    $ wget https://raw.githubusercontent.com/ivarref/ora-600-kdsgrp1-bug/master/src/main/java/Bug.java

    Then compile and run the application to reproduce the bug:

    $ javac -cp /u01/app/oracle/product/12.1.0.2/db_1/jdbc/lib/ojdbc7.jar Bug.java && java -cp /u01/app/oracle/product/12.1.0.2/db_1/jdbc/lib/ojdbc7.jar:. Bug

## Number of iterations

I usually got the error well before 200 iterations.

## Sample output ORA-600 kdsgrp1

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


## Sample output ORA-600 6033

    (...)
    >>> 100
    >>> 101
    >>> 102
    >>> 103
    >>> 104
    >>> 105
    >>> 106
    java.sql.SQLException: ORA-00600: internal error code, arguments: [6033], [], [], [], [], [], [], [], [], [], [], []
    ORA-06512: at "SYS.DBMS_AQ", line 366
    ORA-06512: at line 1

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
        at Bug.dequeSingle(Bug.java:97)
        at Bug.runIt(Bug.java:50)
        at Bug.main(Bug.java:26)

