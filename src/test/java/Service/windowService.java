package Service;

import Domain.PerformanceData;

import java.io.IOException;

public class windowService {
    public PerformanceData getPerformanceData() throws IOException {
//        PerformanceData performanceData = new PerformanceData(getCpuUsage(), getMemoryUsage(), getIOUtil(),packets,bytes);
        PerformanceData performanceData = new PerformanceData((float) (Math.random() * 12), (float) (Math.random() * 20), (float) (Math.random() * 15),(float) Math.random() * 12,(float) Math.random() * 12);
        //        performanceDataRepository.save(performanceData);
        return performanceData;
    }
}
