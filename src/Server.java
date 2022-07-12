import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import java.awt.Dimension;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.SimpleFormatter;

public class Server extends JFrame{

    String[] columnNames = {
        "順位","ユーザー名","スコア","経過時間"
    };

    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
    JTable table = new JTable(tableModel);
    JScrollPane sp = new JScrollPane(table);
    JPanel panel = new JPanel();

    ServerSocket ss = new ServerSocket(10000);

    static Logger logger = Logger.getLogger("MyMogura_log");

    Server(String title) throws IOException{
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setTitle(title);
        setBounds(10,10,500,500);
        sp.setPreferredSize(new Dimension(400,400));
        panel.add(sp);
        getContentPane().add(panel);

        while (true) {
            Socket socket = ss.accept();
            Thread thread = new Thread(new Session(socket));
            thread.start();
        }
    }

    private class Session extends NetBase implements Runnable {

        private Session(Socket socket) throws IOException {
            super(socket);
            logger.log(Level.FINE,"Server::Session::Session() call");
        }

        public void run() {
            logger.log(Level.FINE,"Server::Session::run() call");
            String name = readLine();
            String score = readLine();
            String time = readLine();

            String [] array = {
                "1位",name,score, time+"秒"
            };

            if(tableModel.getRowCount()==0){
                tableModel.addRow(array);
            }else{
                int target = Integer.valueOf(score);
                int rowcount = tableModel.getRowCount();

                for(int i = 0; i < rowcount; i++){
                    if(target > Integer.valueOf((tableModel.getValueAt(i, 2).toString()))){
                        array[0]= Integer.toString(i+1)+"位";
                        tableModel.insertRow(i,array);
                        for(int j = 0; j < rowcount+1; j++){
                            tableModel.setValueAt(Integer.toString(j+1)+"位", j, 0);
                        }
                        break;
                    }

                    if(i==rowcount-1){
                        array[0] = Integer.toString(i+2)+"位";
                        tableModel.insertRow(i+1, array);
                    }
                }
            }
        }
    }
    public static void main(String[] args) throws IOException{
        logger.setLevel(Level.FINEST);
        FileHandler fileHandler = new FileHandler("./Server.log");
        logger.addHandler(fileHandler);

        Formatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);

        logger.log(Level.FINE,"Server::main() call");

        new Server("Server");
    }
}
