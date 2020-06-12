package testfx;


/**
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates. * All rights reserved. Use is subject to license terms.
 */

import Service.PerformanceService;
import Utils.GenericPair;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;

/**
 * A simulated stock line chart. * * @see javafx.scene.chart.Chart * @see javafx.scene.chart.LineChart * @see javafx.scene.chart.NumberAxis * @see javafx.scene.chart.XYChart
 */
public class testFx2 extends Application {
    private XYChart.Series<Number, Number> hourDataSeries;
    private XYChart.Series<Number, Number> minuteDataSeries;
    private NumberAxis xAxis;
    private Timeline animation;
    private double hours = 0;
    private double minutes = 0;
    private double timeInHours = 0;
    private double prevY = 0;
    private double y = 0;

    private void init(Stage primaryStage) throws IOException {
        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        root.getChildren().add(createChart());        // create timeline to add new data every 60th of second
//        root.getChildren().add(createChart());


        animation = new Timeline();
        animation.getKeyFrames().add(new KeyFrame(Duration.millis(1000 / 60), new EventHandler<ActionEvent>() {
            public void handle(ActionEvent actionEvent) {                // 6 minutes data per frame
                for (int count = 0; count < 6; count++) {
                    nextTime();
                    plotTime();
                }
            }
        }));
        animation.setCycleCount(Animation.INDEFINITE);
    }

    protected LineChart<Number, Number> createChart() throws IOException {
        xAxis = new NumberAxis(0, 24, 3);
        final NumberAxis yAxis = new NumberAxis(0, 100, 10);
        final LineChart<Number, Number> lc = new LineChart<Number, Number>(xAxis, yAxis);        // setup chart
        lc.setId("lineStockDemo");
        lc.setCreateSymbols(false);
        lc.setAnimated(false);
        lc.setLegendVisible(false);
        lc.setTitle("CPU Utilization");
        xAxis.setLabel("Time");
        xAxis.setForceZeroInRange(false);
        yAxis.setLabel("Utilization");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, "%"));        // add starting data
        hourDataSeries = new XYChart.Series<Number, Number>();
        hourDataSeries.setName("Hourly Data");
        minuteDataSeries = new XYChart.Series<Number, Number>();
        minuteDataSeries.setName("Minute Data");        // create some starting data
        hourDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, prevY));
        minuteDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, prevY));
        for (double m = 0; m < (60); m++) {
            nextTime();
            plotTimePoint();
        }
//        lc.getData().add(minuteDataSeries);
        lc.getData().add(hourDataSeries);
        return lc;
    }

    protected LineChart<Number, Number> createCPUChart() {
        xAxis = new NumberAxis(0, 24, 3);
        final NumberAxis yAxis = new NumberAxis(0, 100, 10);
        final LineChart<Number, Number> lc = new LineChart<Number, Number>(xAxis, yAxis);        // setup chart
        lc.setId("lineStockDemo");
        lc.setCreateSymbols(false);
        lc.setAnimated(false);
        lc.setLegendVisible(false);
        lc.setTitle("CPU Utilization");
        xAxis.setLabel("Time");
        xAxis.setForceZeroInRange(false);
        yAxis.setLabel("Utilization");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, "%"));        // add starting data
        hourDataSeries = new XYChart.Series<Number, Number>();
        hourDataSeries.setName("Hourly Data");
        minuteDataSeries = new XYChart.Series<Number, Number>();
        minuteDataSeries.setName("Minute Data");        // create some starting data
        hourDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, prevY));
        minuteDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, prevY));
        for (double m = 0; m < (60); m++) {
            nextTime();
            plotTime();
        }
        lc.getData().add(minuteDataSeries);
//        lc.getData().add(hourDataSeries);
        return lc;
    }

    private void nextTime() {
        if (minutes == 59) {
            hours++;
            minutes = 0;
        } else {
            minutes++;
        }
        timeInHours = hours + ((1d / 60d) * minutes);
    }

    private void plotTime() {
        if ((timeInHours % 1) == 0) {            // change of hour
            double oldY = y;
            y = prevY - 10 + (Math.random() * 20);
            prevY = oldY;
            while (y < 10 || y > 90) y = y - 10 + (Math.random() * 20);
            hourDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, prevY));            // after 25hours delete old data
            if (timeInHours > 25)
                hourDataSeries.getData().remove(0);            // every hour after 24 move range 1 hour
            if (timeInHours > 24) {
                xAxis.setLowerBound(xAxis.getLowerBound() + 1);
                xAxis.setUpperBound(xAxis.getUpperBound() + 1);
            }
        }
        double min = (timeInHours % 1);
        double randomPickVariance = Math.random();
        if (randomPickVariance < 0.3) {
            double minY = prevY + ((y - prevY) * min) - 4 + (Math.random() * 8);
            minuteDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, minY));
        } else if (randomPickVariance < 0.7) {
            double minY = prevY + ((y - prevY) * min) - 6 + (Math.random() * 12);
            minuteDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, minY));
        } else if (randomPickVariance < 0.95) {
            double minY = prevY + ((y - prevY) * min) - 10 + (Math.random() * 20);
            minuteDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, minY));
        } else {
            double minY = prevY + ((y - prevY) * min) - 15 + (Math.random() * 30);
            minuteDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, minY));
        }        // after 25hours delete old data
        if (timeInHours > 1) minuteDataSeries.getData().remove(0);
    }

    private void plotTimePoint() throws IOException {
        prevY = getCpuUsage()*100;
        hourDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, y));
        if (timeInHours > 1) minuteDataSeries.getData().remove(0);
    }

    public void play() {
        animation.play();
    }

    @Override
    public void stop() {
        animation.pause();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
        play();
    }

    public static void main(String[] args) {
        launch(args);
    }

////////////////////////////////////
    private final static int CPU_INTERVAL_TIME = 100;
    private final static int NET_INTERVAL_TIME = 100;
//    private final Log log = LogFactory.getLog(PerformanceService.class);

    private int totalBandWidth;

//    @Autowired
//    private PerformanceDataRepository performanceDataRepository;


    public GenericPair<Long, Long> calculateIdleAndTotal() throws IOException {
        //空闲时间，总时间
        long idleCpuTime = -1, totalCpuTime = -1;
        //执行命令
        Process process = Runtime.getRuntime().exec("cat /proc/stat");
        //获取stat文件
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            //选取以CPU字样开头的行
            if (line.startsWith("cpu")) {
                line = line.trim();
                //以空格为分隔符
                String[] temp = line.split("\\s+");
                //获取第四个数据：空闲时间
                idleCpuTime = Long.parseLong(temp[4]);
                for (String s : temp) {
                    //获取总时间
                    if (!s.equals("cpu")) {
                        totalCpuTime += Long.parseLong(s);
                    }
                }
                break;
            }
        }
        bufferedReader.close();
        process.destroy();
        return new GenericPair(idleCpuTime, totalCpuTime);
    }

    public float getCpuUsage() throws IOException {
        float cpuUsage = -1;
        GenericPair<Long, Long> firstPair = calculateIdleAndTotal();
        try {
            Thread.sleep(CPU_INTERVAL_TIME);
        } catch (InterruptedException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
//            log.error(e.getMessage());
//            log.error(sw.toString());
        }
        GenericPair<Long, Long> secondPair = calculateIdleAndTotal();
        long idleCpuTime1 = firstPair.getFirst();
        long totalCpuTime1 = firstPair.getSecond();
        long idleCpuTime2 = secondPair.getFirst();
        long totalCpuTime2 = secondPair.getSecond();
        //计算CPU使用率
        if (idleCpuTime1 != -1 && totalCpuTime1 != -1 && idleCpuTime2 != -1 && totalCpuTime2 != -1) {
            cpuUsage = 1 - (float) (idleCpuTime2 - idleCpuTime1) / (float) (totalCpuTime2 - totalCpuTime1);
        }
        return cpuUsage;
    }

    public float getMemoryUsage() throws IOException {
        float memoryUsage = -1;
        Process process = Runtime.getRuntime().exec("cat /proc/meminfo");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        long totalMemory = 0, freeMemory = 0;
        for (int i = 0; (line = bufferedReader.readLine()) != null && i < 2; ++i) {
            String[] temp = line.split("\\s+");
            if (temp[0].startsWith("MemTotal")) totalMemory = Long.parseLong(temp[1]);
            else if (temp[0].startsWith("MemFree")) freeMemory = Long.parseLong(temp[1]);
            memoryUsage = 1 - (float) freeMemory / (float) totalMemory;
        }
        return memoryUsage;
    }

    public float getIOUsage() throws IOException {
        float ioUsage = -1;
        Process process = Runtime.getRuntime().exec("iostat -dx");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        for (int i = 0; (line = bufferedReader.readLine()) != null; ++i) {
            //从第四行开始读
            if (i >= 3) {
                String[] temp = line.split("\\s+");
                if (temp.length > 1) {
                    float util = Float.parseFloat(temp[temp.length - 1]);
                    ioUsage = (ioUsage > util) ? ioUsage : util;
                }
            }
        }
        ioUsage /= 100;
        bufferedReader.close();
        process.destroy();
        return ioUsage;
    }

    public float getCpuUsageOld() throws IOException {
        float cpuIdle = -1;
        Process process = Runtime.getRuntime().exec("iostat -c");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        for (int i = 0; (line = bufferedReader.readLine()) != null; ++i) {
            //从第四行开始读
            if (i >= 3) {
                String[] temp = line.split("\\s+");
                if (temp.length > 1) {
                    float idle = Float.parseFloat(temp[temp.length - 1]);
                    cpuIdle = (cpuIdle > idle) ? cpuIdle : idle;
                }
            }
        }
        if (cpuIdle == -1) return -1;
        cpuIdle /= 100;
        bufferedReader.close();
        process.destroy();
        return 1.00f - cpuIdle;
    }

    public float getNetUsage() throws IOException {
        float netUsage = -1;
        Process process1, process2;
        Runtime runtime = Runtime.getRuntime();
        String command = "cat /proc/net/dev";
        //第一次采集流量数据
        long startTime = System.currentTimeMillis();
        process1 = runtime.exec(command);
        BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(process1.getInputStream()));
        String line;
        long inSize1 = 0, outSize1 = 0;
        while ((line = bufferedReader1.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("ens33") || line.startsWith("eth0")) {
                String[] temp = line.split("\\s+");
                inSize1 = Long.parseLong(temp[1]); //Receive bytes,单位为Byte
                outSize1 = Long.parseLong(temp[9]);//Transmit bytes,单位为Byte
//                log.info("receive:"+inSize1+"transmit:"+outSize1);
                break;
            }
        }
        bufferedReader1.close();
        process1.destroy();
        try {
            Thread.sleep(NET_INTERVAL_TIME);
        } catch (InterruptedException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
        }
        //第二次采集流量数据
        long endTime = System.currentTimeMillis();
        process2 = runtime.exec(command);
        BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));
        long inSize2 = 0, outSize2 = 0;
        while ((line = bufferedReader2.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("ens33") || line.startsWith("eth0")) {
                String[] temp = line.split("\\s+");
                inSize2 = Long.parseLong(temp[1]);
                outSize2 = Long.parseLong(temp[9]);
//                log.info("receive:"+inSize2+"transmit:"+outSize2);
                break;
            }
        }
        if (inSize1 != 0 && outSize1 != 0 && inSize2 != 0 && outSize2 != 0) {
            //
            float interval = (float) (endTime - startTime) / 1000;
            //网口传输速度,单位为bps
            float curRate = (float) (inSize2 - inSize1 + outSize2 - outSize1) * 8 / (interval * 1000000);
            netUsage = curRate / totalBandWidth;
        }
        bufferedReader2.close();
        process2.destroy();
        return netUsage;
    }
}
