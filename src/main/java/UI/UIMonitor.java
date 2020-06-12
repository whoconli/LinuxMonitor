package UI;

//


import DataStructure.PerformanceData;
import Monitor.PerformanceService;
import Monitor.dataConnect;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class UIMonitor extends Application {

    private static final int MAX_DATA_POINTS = 50;
    private int xSeriesData = 0;
    private XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Number> dataQ = new ConcurrentLinkedQueue<Number>();

    private int flag = 0;
    private boolean stop = false;
    private boolean readData = true;

    private NumberAxis xAxis;
    private NumberAxis yAxis; 
    private PerformanceService monitor;
    private PerformanceData dataStructure;

    private float  cpu,mem,io,pack;
//    private String insert = "insert into monitorData(cpuUtil,memUtil,ioUtil,packets) values('"+cpu+"','"+mem+"','"+io+"','"+pack+"')";
    private String drop = "truncate table monitorData";
    private String dele = "delete from monitorData limit 1";
    private String select = "SELECT * FROM monitorData order by id DESC limit " + MAX_DATA_POINTS;
    private Statement state;
    private dataConnect con;

    private void init(Stage primaryStage) throws SQLException, ClassNotFoundException {

        monitor = new PerformanceService(); // 创建监控器实例

        con = new dataConnect();
        state = con.getConnect();

        state.executeUpdate(drop);

        //设置坐标属性
        xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);
        xAxis.setLabel("Time");
        xAxis.setForceZeroInRange(false);
        yAxis = new NumberAxis();
        yAxis.setLabel("Utilization");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, "%"));
        
        // 创建 LineChart并设置属性
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis) {
            // Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
            }
        };

        lineChart.setAnimated(false);
        lineChart.setTitle("           Monitor");
        lineChart.setHorizontalGridLinesVisible(true);


        // 设置监控序列名字
        series.setName("CPU ");

        lineChart.getData().addAll(series);

        //创建group放置对应器件
        Group g = new Group();
        g.getChildren().add(lineChart);


        Button stopButton = new Button("停/开");
        Button cpuButton = new Button("CPU");
        Button memButton = new Button("MEM");
        Button ioButton = new Button("IO");
        Button netButton = new Button("NET");
        Button readButton = new Button("Read");


        g.getChildren().add(stopButton);
        g.getChildren().add(cpuButton);
        g.getChildren().add(memButton);
        g.getChildren().add(ioButton);
        g.getChildren().add(netButton);
        g.getChildren().add(readButton);
//        stopButton.setLayoutX(0);
//        stopButton.setLayoutY(0);
//        cpuButton.setLayoutX(47);
//        cpuButton.setLayoutY(0);
//        memButton.setLayoutX(87);
//        memButton.setLayoutY(0);
//        ioButton.setLayoutX(132);
//        ioButton.setLayoutY(0);
//        netButton.setLayoutX(162);
//        netButton.setLayoutY(0);   // windows
        stopButton.setLayoutX(0);
        stopButton.setLayoutY(0);
        cpuButton.setLayoutX(53);
        cpuButton.setLayoutY(0);
        memButton.setLayoutX(93);
        memButton.setLayoutY(0);
        ioButton.setLayoutX(133);
        ioButton.setLayoutY(0);
        netButton.setLayoutX(166);
        netButton.setLayoutY(0);
        readButton.setLayoutX(205);
        readButton.setLayoutY(0);

        /**
         * 监控stop/start按钮，摁一下停/开间循环
         */
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stop = !stop;
            }
        });


        /**
         * 监控read按钮，摁一下停/开间循环
         */
        readButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stop = true;
                readData = true; //这里是同步的两个东西
                System.out.println("read!");
            }
        });

        /**
         * 触发按钮，切换为监控CPU
         */
        cpuButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                flag = 0;
                series.getData().remove(0, series.getData().size());
                series.setName("CPU");
                yAxis.setLabel("Utilization");
                yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, "%"));
                dataQ.clear();
                System.out.println(flag);
                if(readData){
                    try {
                        readDateFromMysql();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        });
        
        /**
         * 触发按钮，切换为监控Mem
         */
        memButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                flag = 1;
                series.getData().remove(0, series.getData().size());
                series.setName("MEM");
                yAxis.setLabel("Utilization");
                yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, "%"));
                dataQ.clear();
                System.out.println(flag);
                if(readData){
                    try {
                        readDateFromMysql();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        });

        /**
         * 触发按钮，切换为监控IO
         */
        ioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                flag = 2;
                series.getData().remove(0, series.getData().size());
                series.setName("IO");
                yAxis.setLabel("Utilization");
                yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, "%"));
                dataQ.clear();
                System.out.println(flag);
                if(readData){
                    try {
                        readDateFromMysql();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        });

        /**
         * 触发按钮，切换为监控NET
         */
        netButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                flag = 3;
                series.getData().remove(0, series.getData().size());
                series.setName("NET");
                yAxis.setLabel("Number");
                yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, null));
                dataQ.clear();
                System.out.println(flag);
                if(readData){
                    try {
                        readDateFromMysql();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        });
        
        primaryStage.setScene(new Scene(g));
    }

    public void readDateFromMysql() throws SQLException {
        ResultSet rs;
        try{
            rs = state.executeQuery(select);
            ArrayList<Float> tmp = new ArrayList<Float>();
            while (rs.next()) {
                tmp.add(Float.parseFloat(rs.getString(flag+2)));
            }
            for(int i = tmp.size()-1;i>=0;i--){
                dataQ.add(tmp.get(i));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }



    @Override
    public void start(Stage stage) throws SQLException, ClassNotFoundException {
        stage.setTitle("Linux Monitor");
        init(stage);
        stage.show();

        executor = Executors.newCachedThreadPool(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });

        UIMonitor.AddToQueue addToQueue = new UIMonitor.AddToQueue();
        executor.execute(addToQueue);
        //-- Prepare Timeline
        prepareTimeline();
    }

    private class AddToQueue implements Runnable {
        public void run() {
            try {
                // add a item of random data to queue
                if(!stop) {
                    System.out.println("monitoring");
                    dataStructure = monitor.getPerformanceData();
                    setData(dataStructure);
                    if (flag == 0) {
                        dataQ.add(cpu);
                    } else if (flag == 1) {
                        dataQ.add(mem);
                    } else if (flag == 2) {
                        dataQ.add(io);
                    } else if (flag == 3) {
                        dataQ.add(pack);
                    }
//                    System.out.println("cpu-->"+cpu);
                    String insert = "insert into monitorData(cpuUtil,memUtil,ioUtil,packets) values('"+cpu+"','"+mem+"','"+io+"','"+pack+"')";

                    state.executeUpdate(insert);
//                    System.out.println("inset!!");
                }
                else{
                    System.out.println("stop!!!");
                }

                Thread.sleep(500);
                executor.execute(this);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    //-- Timeline gets called in the JavaFX Main thread
    private void prepareTimeline() {
        // Every frame to take any data from queue and add to chart
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                addDataToSeries();
            }
        }.start();
    }

    private void addDataToSeries() {
        for (int i = 0; i < 20; i++) { //-- add 20 numbers to the plot+
            if (dataQ.isEmpty()) break;
            series.getData().add(new XYChart.Data<Number, Number>(xSeriesData++, dataQ.remove()));
        }
        // remove points to keep us at no more than MAX_DATA_POINTS
        if (series.getData().size() > MAX_DATA_POINTS) {
            series.getData().remove(0, series.getData().size() - MAX_DATA_POINTS);
//            System.out.println("iii->"+(series.getData().size() - MAX_DATA_POINTS));
            try {
//                state.executeQuery(select);
                state.executeUpdate(dele);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }


        // update
        xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
        xAxis.setUpperBound(xSeriesData - 1);
    }

    private void setData(PerformanceData dataStructure){
        this.cpu = dataStructure.getCpuUsage();
        this.mem = dataStructure.getMemoryUsage();
        this.io = dataStructure.getIOUsage();
        this.pack = dataStructure.getPackets();
    }


    public static void main(String[] args) {
        launch(args);
    }
}


