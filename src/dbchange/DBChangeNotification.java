package dbchange;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.ResourceBundle;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleDriver;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.dcn.DatabaseChangeEvent;
import oracle.jdbc.dcn.DatabaseChangeListener;
import oracle.jdbc.dcn.DatabaseChangeRegistration;
 
//https://docs.oracle.com/cd/E16338_01/java.112/b56281/dbchgnf.htm
public class DBChangeNotification {

	static String URL;
	static String USER;
	static String PASS;
	public static void main(String[] argv) {
		ResourceBundle rb = ResourceBundle.getBundle("prop");
		URL = rb.getString("URL");
		USER = rb.getString("USER");
		PASS = rb.getString("PASS");
	    DBChangeNotification demo = new DBChangeNotification();
	    try
	    {
	      	demo.run();
	    }
	    catch(SQLException mainSQLException )
	    {
	      	mainSQLException.printStackTrace();
	    }
	}   
	    
    void run() throws SQLException
    {
        OracleConnection conn = connect();
          
        // first step: create a registration on the server:
        Properties prop = new Properties();
        
        // if connected through the VPN, you need to provide the TCP address of the client.
        // For example:
        // prop.setProperty(OracleConnection.NTF_LOCAL_HOST,"14.14.13.12");
   	
        // Ask the server to send the ROWIDs as part of the DCN events (small performance
        // cost):
        prop.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS, "true");
	    // 
	    //Set the DCN_QUERY_CHANGE_NOTIFICATION option for query registration with finer granularity.
	    prop.setProperty(OracleConnection.DCN_QUERY_CHANGE_NOTIFICATION, "true");
	    
        // The following operation does a roundtrip to the database to create a new
        // registration for DCN. It sends the client address (ip address and port) that
        // the server will use to connect to the client and send the notification
        // when necessary. Note that for now the registration is empty (we haven't registered
        // any table). This also opens a new thread in the drivers. This thread will be
        // dedicated to DCN (accept connection to the server and dispatch the events to 
        // the listeners).
        DatabaseChangeRegistration dcr = conn.registerDatabaseChangeNotification(prop);
        
        try
        {
            // add the listenerr:
            DCNDemoListener list = new DCNDemoListener(this);
            dcr.addListener(list);
             
            // second step: add objects in the registration:
            Statement stmt = conn.createStatement();
            // associate the statement with the registration:
            ((OracleStatement)stmt).setDatabaseChangeRegistration(dcr);
            ResultSet rs = stmt.executeQuery("select ID from USER_DATA");
            while (rs.next())
            {}
            String[] tableNames = dcr.getTables();
            for(int i=0;i<tableNames.length;i++)
            	System.out.println(tableNames[i]+" is part of the registration.");
            rs.close();
            stmt.close();
        }
        catch(SQLException ex)
        {
            // if an exception occurs, we need to close the registration in order
            // to interrupt the thread otherwise it will be hanging around.
            if(conn != null)
              	conn.unregisterDatabaseChangeNotification(dcr);
            throw ex;
        }
        finally
        {
            try
            {
                // Note that we close the connection!
                conn.close();
            }
            catch(Exception innerex){ innerex.printStackTrace(); }
        }
        
        synchronized( this ) 
        {	/*
            // The following code modifies the dept table and commits:
            try
            {
                OracleConnection conn2 = connect();
                conn2.setAutoCommit(false);
                Statement stmt2 = conn2.createStatement();
                //stmt2.executeUpdate("delete from USER_DATA where ID='6'", Statement.RETURN_GENERATED_KEYS);
                //ResultSet autoGeneratedKey = stmt2.getGeneratedKeys();
                //if(autoGeneratedKey.next())

                stmt2.executeUpdate("insert into USER_DATA (ID,NAME,BIRTH_YEAR,BIRTH_MONTH,BIRTH_DAY,SEX) values ('6','テスト　プリン６','2016','6','6','6')", Statement.RETURN_GENERATED_KEYS);
                ResultSet autoGeneratedKey = stmt2.getGeneratedKeys();
                if(autoGeneratedKey.next())
                  	System.out.println("inserted one row with ROWID="+autoGeneratedKey.getString(1));

                //stmt2.executeUpdate("delete from USER_DATA where ID='7'", Statement.RETURN_GENERATED_KEYS);
                //autoGeneratedKey = stmt2.getGeneratedKeys();
                //if(autoGeneratedKey.next())

                stmt2.executeUpdate("insert into USER_DATA (ID,NAME,BIRTH_YEAR,BIRTH_MONTH,BIRTH_DAY,SEX) values ('7','テスト　プリン７','2017','7','7','7')", Statement.RETURN_GENERATED_KEYS);
                autoGeneratedKey = stmt2.getGeneratedKeys();
                if(autoGeneratedKey.next())
                  	System.out.println("inserted one row with ROWID="+autoGeneratedKey.getString(1));
                stmt2.close();
                conn2.commit();
                conn2.close();
            }
            catch(SQLException ex) { ex.printStackTrace(); }
            */
   
            // wait until we get the event
            try { 
            	this.wait();
            } catch( InterruptedException ie ) {}
        }
        
        // At the end: close the registration (comment out these 3 lines in order
        // to leave the registration open).
        OracleConnection conn3 = connect();
        conn3.unregisterDatabaseChangeNotification(dcr);
        conn3.close();
    }
    
    /**
     * Creates a connection the database.
     */
    OracleConnection connect() throws SQLException
    {
        OracleDriver dr = new OracleDriver();
        Properties prop = new Properties();
        prop.setProperty("user",DBChangeNotification.USER);
        prop.setProperty("password",DBChangeNotification.PASS);
        
        return (OracleConnection)dr.connect(DBChangeNotification.URL, prop);
    }			    
}

/**
 * DCN listener: it prints out the event details in stdout.
 */
class DCNDemoListener implements DatabaseChangeListener
{
    DBChangeNotification demo;
    DCNDemoListener(DBChangeNotification dem)
    { 
        demo = dem;
    }
    public void onDatabaseChangeNotification(DatabaseChangeEvent e)
    {
        Thread t = Thread.currentThread();
        System.out.println("DCNDemoListener: got an event ("+this+" running on thread "+t+")");
        System.out.println(e.toString());
        //synchronized( demo ){ 
        //	demo.notify();
        //}
    }
}