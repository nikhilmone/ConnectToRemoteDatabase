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
