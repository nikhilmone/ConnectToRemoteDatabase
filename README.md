ConnectToRemoteDatabase
=======================

Connect to remote database in OpenShift :

Here is a test I tried :

####1 : Created 2 applications `application1` and `application2` (JBoss EAP Cartridge)

####2 : Added `mysql` cartridge to `application1` application.

```

$ rhc app show application1 --gears
ID                       State   Cartridges             Size  SSH URL
------------------------ ------- ---------------------- ----- -------------------------------------------------------------------
5304b6385973cab04d00017e started jbosseap-6 haproxy-1.4 small 5304b6385973cab04d00017e@application1-testuser.rhcloud.com
5304b72d4382ecd9f3000020 started mysql-5.5              small 5304b72d4382ecd9f3000020@5304b72d4382ecd9f3000020-testuser.rhcloud.com


$ rhc app show application2 --gears
ID                       State   Cartridges Size  SSH URL
------------------------ ------- ---------- ----- ---------------------------------------------------
5304b8215973ca96b5000001 started jbosseap-6 small 5304b8215973ca96b5000001@application2-testuser.rhcloud.com

```


####3 : I ssh to `application1` and then went to mysql prompt and created a table `author`. 

```

$ rhc ssh application1

[application1-testuser.rhcloud.com 5304b6385973cab04d00017e]\> mysql

mysql> show databases;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| application1           |
| performance_schema |
+--------------------+
4 rows in set (0.00 sec)

mysql> use application1
Database changed
mysql> show tables;
Empty set (0.00 sec)

mysql> create table authors (id INT, name VARCHAR(30), rating INT);
Query OK, 0 rows affected (0.16 sec)

mysql> show tables;
+--------------------+
| Tables_in_application1 |
+--------------------+
| authors            |
+--------------------+
1 row in set (0.00 sec)

mysql> insert into authors (id,name,rating) values(1,"Songs of Ice and Fire",9);
Query OK, 1 row affected (0.07 sec)

mysql> insert into authors (id,name,rating) values(2,"Midnight's Children",8);
Query OK, 1 row affected (0.02 sec)

mysql> select * from authors;
+------+-----------------------+--------+
| id   | name                  | rating |
+------+-----------------------+--------+
|    1 | Songs of Ice and Fire |      9 |
|    2 | Midnight's Children   |      8 |
+------+-----------------------+--------+
2 rows in set (0.00 sec)


```

####4 : Now I will write a standalone program on `application2` , which will query the database `application1`

```

$ rhc ssh application2

> cd app-root/data/
> vi ConnectDBMysqlLocal.java
```

The code is as followed :

```

import java.sql.*;

public class ConnectDBMysqlLocal {

	public ConnectDBMysqlLocal() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		
		String host = "jdbc:mysql://5304b72d4382ecd9f3000020-testuser.rhcloud.com:43426/application1";
		String user = "adminJx6ftlB";
		String password = "KvyUfSCHmxpu";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(host, user, password);

                        System.out.println("\n\t The connection class name is : " + conn.getClass().getName());
                        System.out.println("\n\n\t GOT Connection at: "+new java.util.Date());
                        Statement stmt = (Statement) conn.createStatement();
            		ResultSet rs = stmt.executeQuery("select * from authors");
			System.out.println("\n\t###########################################");
            		System.out.println("\n\tid"+"\tname"+"\t\t\trating");
            		while (rs.next()) {
               			int id = rs.getInt("id");
              			String name = rs.getString("name");
               			String rating = rs.getString("rating");
               			System.out.println("\n\t"+id+"\t"+name+"\t"+rating);
           		 }
			System.out.println("\n\t###########################################");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	     finally{

	    	 if(conn!=null)
	    	 {
	    		 System.out.println("\n\tfinally{} if(con!=null) ");
	    		 try{ conn.close(); 
			      System.out.println("\n\t Closing the connection !!");
				} catch(Exception e){ e.printStackTrace(); }

	    	 }
	     }
	System.out.println("\n\t Successfully Got the Connection ...Exiting the program");;
	}
}

```

Now here we need to take care how you create the database url :

I created above from :


```
rhc ssh application1

> env | grep -i mysql
OPENSHIFT_MYSQL_DB_PORT=43426
OPENSHIFT_MYSQL_DB_HOST=5304b72d4382ecd9f3000020-testuser.rhcloud.com
OPENSHIFT_MYSQL_DB_PASSWORD=KvyUfSCHmxpu
OPENSHIFT_MYSQL_DB_GEAR_UUID=5304b72d4382ecd9f3000020
OPENSHIFT_MYSQL_DB_USERNAME=adminJx6ftlB
OPENSHIFT_MYSQL_DB_URL=mysql://adminJx6ftlB:KvyUfSCHmxpu@5304b72d4382ecd9f3000020-testuser.rhcloud.com:43426/            <<<<< we need to tailor this
```

So I need to modify this to a database url as : "jdbc:mysql://5304b72d4382ecd9f3000020-testuser.rhcloud.com:43426/application1"

I simply removed the credentials and added the schema and `jdbc` protocol here.


####5 : Once done you would need to `scp` the driver jar and put it in the classpath :

```
$ scp mysql-connector-java-5.1.13-bin.jar 5304b8215973ca96b5000001@application2-testuser.rhcloud.com:/var/lib/openshift/5304b8215973ca96b5000001/app-root/data     <<< Should be run from your local machine (You would need to modify the command according to your application)
```
Once scp is done ssh to application `application2` :

```
$ rhc ssh application2
> export CLASSPATH=/var/lib/openshift/5304b8215973ca96b5000001/app-root/data/mysql-connector-java-5.1.13-bin.jar:$CLASSPATH:.:
```

Once done run the program :

```
> java ConnectDBMysqlLocal

	 The connection class name is : com.mysql.jdbc.JDBC4Connection
	 
	 GOT Connection at: Thu Feb 20 03:41:18 EST 2014
	 
	 ###########################################
	 
	 id	name			rating
	 
	 1	Songs of Ice and Fire	9
	 
	 2	Midnight's Children	8
	 
	 ###########################################
	 
	 finally{} if(con!=null) 
	 
	 Closing the connection !!
	 
	 Successfully Got the Connection ...Exiting the program
```


####Note : This is just an example with standalone code. If you have a web/J2EE application (war/ear) the best idea is to create a datasource and then doing the lookups. For eg :

```
                <datasource jndi-name="java:jboss/datasources/MysqlDS"
                    enabled="${mysql.enabled}" use-java-context="true" pool-name="MysqlDS"
                    use-ccm="true">
                    <connection-url>jdbc:mysql://5304b72d4382ecd9f3000020-testuser.rhcloud.com:43426/application1</connection-url>
                    <driver>mysql</driver>
                    <security>
                        <user-name>adminJx6ftlB</user-name>
                        <password>KvyUfSCHmxpu</password>
                    </security>
                    <validation>
                        <check-valid-connection-sql>SELECT 1</check-valid-connection-sql>
                        <background-validation>true</background-validation>
                        <background-validation-millis>60000</background-validation-millis>
                        <!--<validate-on-match>true</validate-on-match>-->
                    </validation>
                    <pool>
                        <flush-strategy>IdleConnections</flush-strategy>
                        <allow-multiple-users/>
                    </pool>
                </datasource>
```
