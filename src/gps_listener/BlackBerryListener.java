package gps_listener;

import java.net.*;
import java.io.*;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;

import java.util.Properties;

public class BlackBerryListener extends Thread{
    private WindowServer windowServer = null;
    private ServerSocket serverListener = null;
    private Socket clientConnection = null;
    private String filePath = null;
    private Properties properties = null;
    private Connection conn = null;
    private Statement comm = null;
    private Calendar date = null;

    public BlackBerryListener(WindowServer windowServer, int port){
        try{
            this.windowServer = windowServer;
            date = Calendar.getInstance();
            filePath = System.getProperty("user.dir");
            properties = new Properties();
            properties.load(new FileInputStream(filePath + "\\config.properties"));
            conn = sqlConnection();

            serverListener = new ServerSocket(port);
            this.windowServer.txtConnections.append("Started BlackBerryListener on port " + port + "\n");

        }catch(Exception e){
            windowServer.txtErrors.append(e.getMessage() + "\n");
        }
    }

    @Override
    public void run(){
        try{

            while(true){
                clientConnection = serverListener.accept();
                new BlackBerryDriver(clientConnection).start();
                clientConnection = null;
            }

        }catch(Exception e){
            windowServer.txtErrors.append(e.getMessage() + "\n");
        }
    }

    class BlackBerryDriver extends Thread{
        private Socket clientConnection = null;
        private BufferedReader dataInput = null;
        private String connectionMessage = null;
        private String clientIPAddress = null;

        public BlackBerryDriver(Socket clientConnection){

            this.clientConnection = clientConnection;
            connectionMessage = clientConnection + "";
            clientIPAddress = clientConnection.getInetAddress().getHostAddress();
            windowServer.txtConnections.append(connectionMessage + " - CONNECTED\n");

        }

        @Override
        public void run(){
            try{
                dataInput = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));

                while(true){
                    String data = dataInput.readLine();
                    if(data!=null){
                        if(data.trim().length() > 0) {
                            saveDataIntoDB(data);
                            windowServer.txtMessages.append(data + "\n");
                        }else{
                            break;
                        }
                    }else{
                        break;
                    }
                }

                dataInput.close();
                this.clientConnection.close();

                windowServer.txtConnections.append(connectionMessage + " - DISCONNECTED\n");

            }catch(Exception e){
                if(e.getMessage().equals("Connection reset")){
                    windowServer.txtConnections.append(connectionMessage + " - DISCONNECTED\n");
                }else{
                    windowServer.txtErrors.append(e.getMessage() + "\n");
                }
            }
        }

        private boolean saveToFile(String data, String locId){
            try{
                BufferedWriter out = new BufferedWriter(new FileWriter(filePath + "\\log\\" + locId + ".dat",true));
                out.write(data);
                out.newLine();
                out.close();
                return true;
            }catch(Exception e){
                windowServer.txtErrors.append(e.getMessage() + "\n");
                return false;
            }
        }

        private void saveDataIntoDB(String data) throws SQLException {
          try{
              data = data.replaceAll("\n", "");
              data = data.replaceAll("\r", "");

              String[] item = data.split(",");

              String LocID = item[0];
              String Longitude = item[1];
              String Latitude = item[2];
              String Altitude = item[3];
              String Speed = item[4];
              String Course = item[5];
              String MobileTime = item[6];
              String MCC = item[7];
              String MNC = item[8];
              String LAC = item[9];
              String CID = item[10];
              String GpsStatus = item[11];


//              if(item[1].trim().indexOf("0.0") != -1){
//                String[] coords = getLastCoords(LocID);
//                Longitude = coords[0];
//                Latitude = coords[1];
//              }

              String TrueTime = DateTime();
              String Location = "";//getAddressXY(Longitude, Latitude);
              String IPAddress = clientConnection.getInetAddress().getHostAddress();
              String IPPort = clientConnection.getPort() + "";

             if(Altitude.equals("NaN")) Altitude = "0.0";
             if(Speed.equals("NaN")) Speed = "0.0";
             if(Course.equals("NaN")) Course = "0.0";

              Statement comm = conn.createStatement();

              String sql = "INSERT INTO ITSM_History (" +
                              "LocID," +
                              "Longitude," +
                              "Latitude," +
                              "Altitude," +
                              "Speed," +
                              "Course," +
                              "Location," +
                              "MMC," +
                              "MNC," +
                              "LAC," +
                              "CID," +
                              "GpsStatus,"+
                              "MobileTime,"+
                              "TrueTime"+
                          ") " +
                          "VALUES (" +
                              "'" + LocID + "'," +
                              "'" + Longitude + "'," +
                              "'" + Latitude + "'," +
                              "'" + Altitude + "'," +
                              "'" + Speed + "'," +
                              "'" + Course + "'," +
                              "'" + Location + "'," +
                              "'" + MCC + "'," +
                              "'" + MNC + "'," +
                              "'" + LAC + "'," +
                              "'" + CID + "'," +
                              "'" + GpsStatus + "'," +
                              "'" + MobileTime + "'," +
                              "'" + TrueTime + "'" +
//                              "'" + IPAddress + "'," +
//                              IPPort +
                          ")";

              comm.execute(sql);
              comm.close();

              windowServer.model.setValueAt("LocID", 0, 0);
              windowServer.model.setValueAt(LocID, 0, 1);
              windowServer.model.setValueAt("Longitude", 1, 0);
              windowServer.model.setValueAt(Longitude, 1, 1);
              windowServer.model.setValueAt("Latitude", 2, 0);
              windowServer.model.setValueAt(Latitude, 2, 1);
              windowServer.model.setValueAt("Altitude", 3, 0);
              windowServer.model.setValueAt(Altitude, 3, 1);
              windowServer.model.setValueAt("Speed", 4, 0);
              windowServer.model.setValueAt(Speed, 4, 1);
              windowServer.model.setValueAt("Course", 5, 0);
              windowServer.model.setValueAt(Course, 5, 1);
              windowServer.model.setValueAt("Location", 6, 0);
              windowServer.model.setValueAt(Location, 6, 1);
              windowServer.model.setValueAt("MCC", 7, 0);
              windowServer.model.setValueAt(MCC, 7, 1);
              windowServer.model.setValueAt("MNC", 8, 0);
              windowServer.model.setValueAt(MNC, 8, 1);
              windowServer.model.setValueAt("IPAddress", 9, 0);
              windowServer.model.setValueAt(IPAddress, 9, 1);
              windowServer.model.setValueAt("IPPort", 10, 0);
              windowServer.model.setValueAt(IPPort, 10, 1);
              windowServer.model.fireTableDataChanged();

              String fileName = LocID + "_" + TrueTime.substring(0,10).replace("/", "");
              saveToFile(data, fileName);

          }catch(SQLException e){
              windowServer.txtErrors.append(e.getMessage() + "\n");
              if(conn.isClosed() || conn==null){
                  conn = sqlServer2005Connection();
                  saveDataIntoDB(data);
              }else{
                  conn.close();
                  conn = sqlServer2005Connection();
                  saveDataIntoDB(data);
              }
          }catch(Exception e){
              windowServer.txtErrors.append(e.getMessage() + "\n");
          }

        }

        private String getAddressXY(String x, String y) {
            try{
                StringBuffer text = new StringBuffer();
                String result = "";

                String url = properties.getProperty("geocoderUrl");
                String param1 = properties.getProperty("geocoderUrlParam1");
                String param2 = properties.getProperty("geocoderUrlParam2");
                url = url + "?" + param1 + "=" + x + "&" + param2 + "=" + y;

                URL page = new URL(url);
                HttpURLConnection urlConn = (HttpURLConnection) page.openConnection();
                urlConn.connect();
                InputStreamReader in = new InputStreamReader((InputStream) urlConn.getContent());
                BufferedReader buff = new BufferedReader(in);
                String line = buff.readLine();

                while(line!=null){
                    text.append(line);
                    line = buff.readLine();
                }

                result = text.toString();

                text = null;
                buff.close();
                buff = null;
                in.close();
                in = null;
                page = null;

                return result;

            }catch(MalformedURLException e) {
                windowServer.txtErrors.append(e.getMessage() + "\n");
                return "";
            }catch(Exception e) {
                windowServer.txtErrors.append(e.getMessage() + "\n");
                return "";

            }
        }
    }

    private String[] getLastCoords(String LocID){
        try{
            String[] result = {"0.0","0.0"};
            Statement comm = conn.createStatement();
            ResultSet data = comm.executeQuery("SELECT TOP 1 Longitude, Latitude " +
                                               "FROM dbo.zzz_BlackBerry WHERE Longitude <> 0.0 " +
                                               "AND LocID = '" + LocID + "' ORDER BY ID DESC");

            while(data.next()){
                result[0] = data.getString("Longitude");
                result[1] = data.getString("Latitude");
            }

            data.close();
            comm.close();

            return result;
        }catch(Exception e){
            windowServer.txtErrors.append(e.getMessage() + "\n");
            return null;
        }
    }

  private String DateTime(){

      String returnValue = "";
      date.setTime(new Date());

      returnValue = date.get(Calendar.YEAR) + "/" +
      (date.get(Calendar.MONTH) + 1) + "/" +
      date.get(Calendar.DAY_OF_MONTH) + " " +
      date.get(Calendar.HOUR_OF_DAY) + ":" +
      date.get(Calendar.MINUTE) + ":" +
      date.get(Calendar.SECOND) + "";

      return returnValue;
}

private Connection sqlConnection(){
        try{
            
            DriverLoad();
            String strconn = "jdbc:mysql://svrba07/ITServiziMobile";
            String user = "ServiziMobile";
            String pwd = "ServiziMobile";
            Connection c = DriverManager.getConnection(strconn, user, pwd);
            return c;

        }catch(Exception ex){
            return null;
        }
    }

static public boolean DriverLoad() {

    try {

      Class.forName("com.mysql.jdbc.Driver");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }

  }
    private Connection sqlServer2005Connection(){
      try{
        Connection con = null;

        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setUser(properties.getProperty("dbUser"));
        ds.setPassword(properties.getProperty("dbPassword"));
        ds.setServerName(properties.getProperty("dbServerName"));
        //ds.setPortNumber(Integer.parseInt(properties.getProperty("dbPortNumber")));
        ds.setDatabaseName(properties.getProperty("dbDatabaseName"));
        con = ds.getConnection();

        return con;

      }catch(Exception e){windowServer.txtErrors.append(e.getMessage() + "\n"); return null;}
    }
}
