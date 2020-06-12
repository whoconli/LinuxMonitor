package Service;

import Domain.NetworkData;
import Utils.GenericPair;
import Domain.PerformanceData;


import java.io.*;


public class PerformanceService {
    private final static int CPU_INTERVAL_TIME = 100;
    private final static int NET_INTERVAL_TIME = 100;
//    private final Log log = LogFactory.getLog(PerformanceService.class);

    private int totalBandWidth = 20;

    private float packets = 0;

    private float bytes = 0;


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
            System.out.println(ioUsage);
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
            System.out.println(line);
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
//        System.out.println("Net = "+ netUsage);
        return netUsage;
    }

//    public void printAll() throws Exception {
//        log.info("CPU usage:" + getCpuUsage());
//        log.info("MEM usage:" + getMemoryUsage());
//        log.info("IO usage:" + getIOUsage());
//        log.info("Net usage:" + getNetUsage());
//    }

    public int getTotalBandWidth() {
        return totalBandWidth;
    }

    public void setTotalBandWidth(int totalBandWidth) {
        this.totalBandWidth = totalBandWidth;
    }

    public PerformanceData getPerformanceData() throws IOException {
//        getNetWork();
//        PerformanceData performanceData = new PerformanceData(getCpuUsage(), getMemoryUsage(), getIOUtil(),packets,bytes);
        PerformanceData performanceData = new PerformanceData((float) (Math.random() * 12), (float) (Math.random() * 20), (float) (Math.random() * 15),(float) Math.random() * 12,(float) Math.random() * 12);
        //        performanceDataRepository.save(performanceData);
        return performanceData;
    }


    public NetworkData getNetData() throws IOException {
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
        long rePackets = 0, reBytes = 0,tranPackets = 0,tranBytes = 0;

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
        long rePackets1 = 0, reBytes1 = 0,tranPackets1 = 0,tranBytes1 = 0;
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
            packets= (float) (rePackets1 - rePackets + tranPackets1 - tranPackets) / (interval);
        }

        if (reBytes1 != 0 && tranBytes1 != 0 && tranBytes != 0 && reBytes != 0) {
            //
            float interval = (float) (endTime - startTime) / 1000; //ms -> s
            float curRate = (float) (reBytes1 - reBytes + tranBytes1 - tranBytes) * 8 / (interval * 1000000);
            bytes = curRate / totalBandWidth;
        }
        bufferedReader2.close();
        process2.destroy();

        NetworkData netWork = new NetworkData(packets,bytes);
        return netWork;
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
        long rePackets = 0, reBytes = 0,tranPackets = 0,tranBytes = 0;

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
        long rePackets1 = 0, reBytes1 = 0,tranPackets1 = 0,tranBytes1 = 0;
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
            packets= (float) (rePackets1 - rePackets + tranPackets1 - tranPackets) / (interval);
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
//        NetworkData netWork = new NetworkData(packets,bytes);
//        return netWork;
    }


    public float getIOUtil() throws IOException {
        float ioUsage = -1;
        Process process = Runtime.getRuntime().exec("iostat -dx");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        for (int i = 0; (line = bufferedReader.readLine()) != null; ++i) {
            //从第四行开始读
            if (line.startsWith("sda")) {
                String[] temp = line.split("\\s+");
                ioUsage = Float.parseFloat(temp[temp.length - 1]);
                break;
            }
//            System.out.println(ioUsage);
        }
//        ioUsage /= 100;
        bufferedReader.close();
        process.destroy();
        return ioUsage;
    }

}

