package Monitor;


import DataStructure.PerformanceData;
import java.io.*;


public class PerformanceService {
    private final static int CPU_INTERVAL_TIME = 100;

    private final static int NET_INTERVAL_TIME = 100;

    private int totalBandWidth = 20;

    private float packets = 0;

    private float bytes = 0;


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
            System.out.println(ioUsage);
        }
        ioUsage /= 100;
        bufferedReader.close();
        process.destroy();
        return ioUsage;
    }


    public int getTotalBandWidth() {
        return totalBandWidth;
    }

    public void setTotalBandWidth(int totalBandWidth) {
        this.totalBandWidth = totalBandWidth;
    }

    /**
     *
     */
    public PerformanceData getPerformanceData() throws IOException {
        getNetWork();
        PerformanceData performanceData = new PerformanceData(getCpuUsage() * 100, getMemoryUsage() * 100, getIOUtil(), packets, bytes);
//        PerformanceData performanceData = new PerformanceData((float) (Math.random() * 12), (float) (Math.random() * 20), (float) (Math.random() * 15),(float) Math.random() * 12,(float) Math.random() * 12);
//        PerformanceData performanceData = new PerformanceData(10,20,30,40,50);
        return performanceData;
    }


    public void getNetWork() throws IOException {
        float netUsage = -1;
        Process process1, process2;
        Runtime runtime = Runtime.getRuntime();
        float packets = 0, bytes = 0;
        String command = "cat /proc/net/dev";
        //第一次采集流量数据
        long startTime = System.currentTimeMillis();
        process1 = runtime.exec(command);
        BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(process1.getInputStream()));
        String line;
        long rePackets = 0, reBytes = 0, tranPackets = 0, tranBytes = 0;

        while ((line = bufferedReader1.readLine()) != null) {
            line = line.trim();
//            System.out.println(line);
            if (line.startsWith("eno1") || line.startsWith("eno2")) {
                String[] temp = line.split("\\s+");
//                System.out.println(temp);
                reBytes += Long.parseLong(temp[1]); //Receive bytes,单位为Byte
                tranBytes += Long.parseLong(temp[9]);//Transmit bytes,单位为Byte
                rePackets += Long.parseLong(temp[2]); //Receive bytes,单位为Byte
                tranPackets += Long.parseLong(temp[10]);//Transmit bytes,单位为Byte
//                break;
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
        long rePackets1 = 0, reBytes1 = 0, tranPackets1 = 0, tranBytes1 = 0;
        while ((line = bufferedReader2.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("eno1") || line.startsWith("eno2")) {
                String[] temp = line.split("\\s+");
                reBytes1 += Long.parseLong(temp[1]); //Receive bytes,单位为Byte
                tranBytes1 += Long.parseLong(temp[9]);//Transmit bytes,单位为Byte
                rePackets1 += Long.parseLong(temp[2]); //Receive bytes,单位为Byte
                tranPackets1 += Long.parseLong(temp[10]);//Transmit bytes,单位为Byte
//                break;
            }
        }
        if (rePackets1 != 0 && rePackets != 0 && tranPackets1 != 0 && tranPackets != 0) {
            //
            float interval = (float) (endTime - startTime) / 1000;//ms -> s
            //网口传输速度,单位为bps
            packets = (float) (rePackets1 - rePackets + tranPackets1 - tranPackets) / (interval);
        }

        if (reBytes1 != 0 && tranBytes1 != 0 && tranBytes != 0 && reBytes != 0) {
            //
            float interval = (float) (endTime - startTime) / 1000; //ms -> s
            float curRate = (float) (reBytes1 - reBytes + tranBytes1 - tranBytes) * 8 / (interval * 1000000);
            bytes = curRate / totalBandWidth;
        }
        bufferedReader2.close();
        process2.destroy();

        this.packets = packets;
        this.bytes = bytes;
    }


    public float getIOUtil() throws IOException {
        float ioUsage = 0;
        for(int k = 0;k<1;k++) {
            Process process = Runtime.getRuntime().exec("iostat -dx 1 2");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            for (int i = 0; (line = bufferedReader.readLine()) != null; ++i) {

                if (line.startsWith("sda")) {
                    String[] temp = line.split("\\s+");
//                    System.out.println(i);
//                    System.out.println(line);
                    ioUsage += Float.parseFloat(temp[temp.length - 1]);
                }
            }
            bufferedReader.close();
            process.destroy();
        }
        ioUsage /= 2;
        return ioUsage;
    }

}

class GenericPair<E, F> {
    private E first;
    private F second;

    public GenericPair() {

    }

    public GenericPair(E first, F second) {
        this.first = first;
        this.second = second;
    }

    public E getFirst() {
        return first;
    }

    public void setFirst(E first) {
        this.first = first;
    }

    public F getSecond() {
        return second;
    }

    public void setSecond(F second) {
        this.second = second;
    }


}