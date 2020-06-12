package uintTest;

import Domain.NetworkData;
import Service.PerformanceService;

import java.io.IOException;

public class Mytest {
    public static void main(String args[]) throws IOException {
        PerformanceService P = new PerformanceService();
        for(int i = 0;i<10;i++){
            float res = P.getIOUsage();
//            System.out.println("Net->"+res.getPackets());
//            System.out.println("Net->"+res.getBytes());
        }
    }
}
