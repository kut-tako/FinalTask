import java.awt.Image;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;

import java.util.TimerTask;
import java.util.Timer;
import java.util.Random;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import java.net.Socket;

import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class MyMogura extends JPanel implements ActionListener {

    static Logger logger = Logger.getLogger("MyMogura_log");

    int score = 0;
    JLabel score_label = new JLabel();
    JLabel leftTime_label = new JLabel();
    JLabel start_label = new JLabel("モグラ画像を選択してください");
    JButton start = new JButton("スタート");

    static JMenuBar menu = new JMenuBar();
    static JMenu file = new JMenu("ファイル");
    static JMenuItem selectPic = new JMenuItem("モグラ画像読込");
    FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG, JPG, GIF, TIF, BMP Images", "png", "jpg", "gif", "tif", "bmp");

    Image newimg;
    Image img;
    static JMenuItem quit = new JMenuItem("終了");
    static JMenu edit = new JMenu("編集");
    static JMenuItem editName = new JMenuItem("ユーザー名変更");
    String PlayerName = null;

    LocalDateTime startTime;
    boolean addFlag = false;
    boolean addmoguraFlag = false;
    mogura m = new mogura();
    mogura m2 = new mogura();
    Random random = new Random();

    long leftTime = 20;

    LocalDateTime nowTime;

    LocalDateTime finishTime;
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分ss秒");
    long playtime;

    public static void main(String[] args) throws IOException{
        FileHandler fileHandler = new FileHandler("./MyMogura.log");
        Formatter formatter = new SimpleFormatter();

        logger.addHandler(fileHandler);
        fileHandler.setFormatter(formatter);
        logger.setLevel(Level.FINEST);

        logger.log(Level.FINE,"MyMogura::main() call");

        JFrame frame = new JFrame("モグラ叩きゲーム");
        frame.setContentPane(new MyMogura());
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setJMenuBar(menu);

    }

    public MyMogura() {

        logger.log(Level.FINE,"MyMogura::MyMogura() call");

        Font font = new Font("MS 明朝", Font.BOLD, 20);
        JFileChooser filechooser = new JFileChooser("./");

        setPreferredSize(new Dimension(500, 500));
        setLayout(null);

        add(score_label);
        score_label.setBounds(0, 50, 100, 20);
        score_label.setText("score:" + Integer.valueOf(score).toString());
        score_label.setFont(font);

        add(leftTime_label);
        leftTime_label.setBounds(0,70,100,20);
        leftTime_label.setText("残り"+leftTime+"秒");
        leftTime_label.setFont(font);

        add(start_label);
        start_label.setBounds(170,50,200,100);

        add(start);
        start.addActionListener(this);
        start.setVisible(false);
        start.setBounds(300, 100, 100, 50);

        menu.add(file);

        file.add(selectPic);
        selectPic.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filechooser.setFileFilter(filter);
                int selected = filechooser.showOpenDialog(selectPic);
                if (selected == JFileChooser.APPROVE_OPTION) {
                    try {
                        String MoguraPath = filechooser.getSelectedFile().toString();
                        img = ImageIO.read(new File(MoguraPath));
                        newimg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                        start.setVisible(true);
                        start_label.setVisible(false);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        file.add(quit);
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int ans = JOptionPane.showConfirmDialog(quit, "本当に終了しますか？", "終了確認", JOptionPane.YES_NO_OPTION);
                if (ans == JOptionPane.OK_OPTION) {
                    System.exit(0);
                }
            }
        });

        menu.add(edit);

        edit.add(editName);
        editName.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PlayerName = JOptionPane.showInputDialog(editName, "名前を入力してください");
            }
        });

    }

    public void actionPerformed(ActionEvent e) {
        logger.log(Level.FINE,"MyMogura::actionPerformed() call");
        logger.log(Level.FINE,"----------------------GameStart--------------------------------------");
        start.setVisible(false);
        selectPic.setVisible(false);
        startTime = LocalDateTime.now();
        if(!addmoguraFlag){
            add(m);
            add(m2);
            addmoguraFlag = true;
        }
        m.init();

    }

    public class mogura extends JLabel {

        mogura() {
            logger.log(Level.FINE,"MyMogura::mogura::mogura() call");
            if(!addFlag){
                addMouseListener(new MouseAdapter(){
                    public void mouseClicked(MouseEvent e){
                        logger.log(Level.FINE,"MyMogura::myListener::mouseClicked() call");
                        incScore();
                        setVisible(false);
                    }
                });
                addFlag = true;
            }
        }

        public void init() {
            logger.log(Level.FINE,"MyMogura::mogura::init() call");
            leftTime = 20;
            leftTime_label.setText("残り"+leftTime+"秒");
            score = 0;
            score_label.setText("score:" + Integer.valueOf(score).toString());
            setBounds(200, 200, 100, 100);
            setTask();
        }

        public void setTask() {
            logger.log(Level.FINE,"MyMogura::mogura::setTask() call");
            Timer timer = new Timer();
            Task task = new Task();

            timer.scheduleAtFixedRate(task, 1000, 1000);
        }

        public class Task extends TimerTask {
            public void run() {
                logger.log(Level.FINE,"MyMogura::Task::run() call");
                int xSize = 20+random.nextInt(80);
                int ySize = 20+random.nextInt(80);
                newimg = img.getScaledInstance(xSize, ySize, Image.SCALE_SMOOTH);
                setBounds(100+random.nextInt(300),100+random.nextInt(300),xSize,ySize);
                nowTime = LocalDateTime.now();
                leftTime = 20 - ChronoUnit.SECONDS.between(startTime, nowTime);
                setVisible(!isVisible());
                leftTime_label.setText("残り"+leftTime+"秒");
                if(leftTime <= 0){
                    leftTime_label.setText("残り0秒");
                    start.setVisible(true);
                    setVisible(false);
                    selectPic.setVisible(true);
                    exportCsv();
                    logger.log(Level.FINE,"----------------------GameFinish--------------------------------------");
                    this.cancel();
                }
            }
        }

        public void paintComponent(Graphics g) {
            logger.log(Level.FINE,"mogura::paint() call");
            g.drawImage(newimg,0,0,null);
        }

    }

    protected void incScore() {
        logger.log(Level.FINE,"MyMogura::incScore() call");
        ++score;
        score_label.setText("score:" + Integer.valueOf(score).toString());
    }

    public void exportCsv() {
        logger.log(Level.FINE,"MyMogura::exportCsv() call");
        finishTime = LocalDateTime.now();
        String date = finishTime.format(format);
        try {
            FileWriter fw = new FileWriter("./UserScore.csv", true);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            playtime = ChronoUnit.SECONDS.between(startTime, finishTime);

            pw.print(date+",");
            pw.print(PlayerName+",");
            pw.print(score+"点,");
            pw.println(playtime+"秒,");

            pw.close();
            sendData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData() throws IOException{
        logger.log(Level.FINE,"MyMogura::sendData() call");
        NetBase net;
        try{
            net = new NetBase(new Socket("localhost", 10000));
            net.println(PlayerName);
            net.println(Integer.valueOf(score).toString());
            net.println(Long.valueOf(playtime).toString());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void paintComponent(Graphics g) {
        logger.log(Level.FINE,"MyMogura::paint() call");
        super.paintComponent(g);
        for (int i = 1 ; i <= 10; ++i) {
            g.drawOval(250-10*i, 250-10*i, 20*i, 20*i);
        }
    }

}
