//



package uintTest;

import Domain.NetworkData;
import Domain.PerformanceData;
import Service.PerformanceService;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import testfx.MemMonitor;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Pane extends Application {

    private static final int MAX_DATA_POINTS = 50;
    private int xSeriesData = 0;
    private XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> series2 = new XYChart.Series<Number, Number>();
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Number> dataQ1 = new ConcurrentLinkedQueue<Number>();
    private ConcurrentLinkedQueue<Number> dataQ2 = new ConcurrentLinkedQueue<Number>();

    private int flag = 0;
    private boolean stop = false;

    private NumberAxis xAxis;
    private PerformanceService service;
    private PerformanceData dataStructure;

    private void init(Stage primaryStage) {

        xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);

//        NumberAxis yAxis = new NumberAxis(0, 50, 10);;
        NumberAxis yAxis = new NumberAxis();//(0, 50, 10);;
        xAxis.setLabel("Time");
        xAxis.setForceZeroInRange(false);
        yAxis.setLabel("Utilization");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, "%"));

        service = new PerformanceService();

        // Create a LineChart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis) {
            // Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
            }
        };

        lineChart.setAnimated(false);
        lineChart.setTitle("Linux Monitor");
        lineChart.setHorizontalGridLinesVisible(true);

//        final LineChart<Number, Number> lineChart2 = new LineChart<Number, Number>(xAxis, yAxis) {
//            // Override to remove symbols on each data point
//            @Override
//            protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
//            }
//        };
//
//        lineChart.setAnimated(false);
////        lineChart.setTitle("Linux Monitor");
//        lineChart.setHorizontalGridLinesVisible(true);

        // Set Name for Series
        series1.setName("CPU ");
        series2.setName("Mem");


//        g.getChildren().addAll(b1,b2,b3,b4,b5);
//        b1.setOnAction(new EventHandler<ActionEvent>(){
//            @Override
//            public void handle(ActionEvent event) {
//                flag++;
//                if(flag >3) flag = 0;
//                dataQ1.clear();
//                System.out.println(flag);
//            }
//
//        });

//        final DropShadow shadow = new DropShadow();
////Adding the shadow when the mouse cursor is on
//        b1.addEventHandler(MouseEvent.MOUSE_ENTERED,
//                new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent e) {
//                        b1.setEffect(shadow);
//                        System.out.println(flag);
//                    }
//                });


        // Add Chart Series
        lineChart.getData().addAll(series1);
//        lineChart2.getData().addAll(series2);


        Group g=new Group();
        g.getChildren().add(lineChart);


        final Button b1=new Button("暂停");
        Button b2=new Button("CPU");
        Button b3=new Button("Mem");
        Button b4=new Button("IO");
        Button b5=new Button("Net");
        g.getChildren().add(b1);
//        g.getChildren().add(b1);
        g.getChildren().add(b2);
        g.getChildren().add(b3);
        g.getChildren().add(b4);
        g.getChildren().add(b5);
        b1.setLayoutX(0);
        b1.setLayoutY(0);
        b2.setLayoutX(40);
        b2.setLayoutY(0);
        b3.setLayoutX(80);
        b3.setLayoutY(0);
        b4.setLayoutX(125);
        b4.setLayoutY(0);
        b5.setLayoutX(155);
        b5.setLayoutY(0);
        b1.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                flag++;
                if(flag >3) flag = 0;
                if(flag == 0){
                    series1.setName("CPU ");
                }
                else if(flag == 1){
                    series1.setName("IO ");
                }
                else if(flag == 2){
                    series1.setName("MEM ");
                }
                else if(flag == 3){
                    series1.setName("Net ");
                }
                dataQ1.clear();
                System.out.println(flag);
            }

        });
        primaryStage.setScene(new Scene(g));
    }


    @Override
    public void start(Stage stage) {
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

        Pane.AddToQueue addToQueue = new Pane.AddToQueue();
        executor.execute(addToQueue);
        //-- Prepare Timeline
        prepareTimeline();
    }

    private class AddToQueue implements Runnable {
        public void run() {
            try {
                // add a item of random data to queue
                dataStructure = service.getPerformanceData();
                if(flag == 0){
                    dataQ1.add(dataStructure.getCpuUsage());
                }
                else if(flag == 1){
                    dataQ1.add(dataStructure.getMemoryUsage());
                }
                else if(flag == 2){
                    dataQ1.add(dataStructure.getIOUsage());
                }
                else if(flag == 3){
                    dataQ1.add(dataStructure.getPackets());
                }


                Thread.sleep(500);
                executor.execute(this);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
            if (dataQ1.isEmpty()) break;
            series1.getData().add(new XYChart.Data<Number, Number>(xSeriesData++, dataQ1.remove()));
//            series2.getData().add(new XYChart.Data<Number, Number>(xSeriesData++, dataQ2.remove()));
        }
        // remove points to keep us at no more than MAX_DATA_POINTS
        if (series1.getData().size() > MAX_DATA_POINTS) {
            series1.getData().remove(0, series1.getData().size() - MAX_DATA_POINTS);
        }
//        if (series2.getData().size() > MAX_DATA_POINTS) {
//            series2.getData().remove(0, series2.getData().size() - MAX_DATA_POINTS);
//        }

        // update
        xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
        xAxis.setUpperBound(xSeriesData - 1);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

