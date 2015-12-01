package gps_listener;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Point;
import java.util.HashMap;

public class WindowServer extends JFrame{
    private final int BlackBerryPort = 2237;
    private Timer timer = null;
    private final JMenuBar menubar = new JMenuBar();
    private final JTabbedPane tabs = new JTabbedPane();
    public JTextArea txtMessages = new JTextArea();
    public JTextArea txtErrors = new JTextArea();
    public JTextArea txtConnections = new JTextArea();
    
    String headers[] = { "Field", "Data" };
    TableModel model = new TableModel(12, headers);
    JTable table = new JTable(model);

    public WindowServer(){
        menubar.add(new FileMenu());
        txtMessages.setEditable(false);
        txtErrors.setEditable(false);
        txtConnections.setEditable(false);
        
        tabs.addTab("Trace", new JScrollPane(txtMessages));
        tabs.addTab("Decoded Trace", new JScrollPane(table));
        tabs.addTab("Errors", new JScrollPane(txtErrors));
        tabs.addTab("Connections", new JScrollPane(txtConnections));        

        timer = new Timer();
        timer.scheduleAtFixedRate(new Task(),0,1000);

        setListenerProperties();

        txtConnections.append("Starting BlackBerryListener on port " + BlackBerryPort + "\n");
        startBlackBarryListener();

    }
    
     private void startBlackBarryListener(){
         new BlackBerryListener(this, BlackBerryPort).start();
    }
    private void setListenerProperties(){
        
        setTitle("GPS Trace Listener");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0,0,600,450);
        setJMenuBar(menubar);
        getContentPane().add(tabs);
        setVisible(true);
    }

    class FileMenu extends JMenu{
        JMenuItem salir = new JMenuItem("Salir");

        public FileMenu(){
            super("File");
            this.add(salir);
        }
    }

    class Task extends TimerTask{
        public Task(){
            super();
        }
        
        public void run(){
            if(txtMessages.getLineCount()>=20){
                txtMessages.setText("");
            }
            if(txtErrors.getLineCount()>=100){
                txtErrors.setText("");
            }
            if(txtConnections.getLineCount()>=100){
                txtConnections.setText("");
            }
        }
    }

}

class TableModel extends AbstractTableModel {
  private HashMap lookup;
  private final int rows;
  private final int columns;
  private final String headers[];

  public TableModel (int rows, String columnHeaders[]) {
    if ((rows < 0) || (columnHeaders == null)) {
      throw new IllegalArgumentException("Invalid row count/columnHeaders");
    }
    this.rows = rows;
    this.columns = columnHeaders.length;
    headers = columnHeaders;
    lookup = new HashMap();
  }

  public int getColumnCount() {
    return columns;
  }

  public int getRowCount() {
    return rows;
  }

  @Override
  public String getColumnName(int column) {
    return headers[column];
  }

  public Object getValueAt(int row, int column) {
    return lookup.get(new Point(row, column));
  }

  @Override
  public void setValueAt(Object value, int row, int column) {
    if ((rows < 0) || (columns < 0)) {
      throw new IllegalArgumentException("Invalid row/column setting");
    }
    if ((row < rows) && (column < columns)) {
      lookup.put(new Point(row, column), value);
    }
  }
}
