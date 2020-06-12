package Domain;

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//
//@Entity
public class PerformanceData {
//    @Id
//    @GeneratedValue
    private int id;
//    @Column
    private float cpuUsage;
//    @Column
    private float memoryUsage;
//    @Column
    private float IOUsage;
//    @Column
    private float NetUsage;
//    @Column
    private long time;

    private float packets;

    private float bytes;

    public PerformanceData() {
        time = System.currentTimeMillis();
    }

    public PerformanceData(float cpuUsage, float memoryUsage, float IOUsage) {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.IOUsage = IOUsage;
        time = System.currentTimeMillis();
    }

    public PerformanceData(float cpuUsage, float memoryUsage, float IOUsage, float packets, float bytes) {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.IOUsage = IOUsage;
        this.packets = packets;
        this.bytes = bytes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(float cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public float getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(float memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public float getIOUsage() {
        return IOUsage;
    }

    public void setIOUsage(float IOUsage) {
        this.IOUsage = IOUsage;
    }

    public float getNetUsage() {
        return NetUsage;
    }

    public void setNetUsage(float netUsage) {
        NetUsage = netUsage;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }



    public float getPackets() {
        return packets;
    }

    public void setPackets(float packets) {
        this.packets = packets;
    }

    public float getBytes() {
        return bytes;
    }

    public void setBytes(float bytes) {
        this.bytes = bytes;
    }
}
