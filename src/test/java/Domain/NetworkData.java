package Domain;

public class NetworkData {


    private float recPackets;

    private float recBytes;

    private float tranPackets;

    private float tranBytes;

    private float packets;

    private float bytes;

    public NetworkData(float recPackets, float recBytes, float tranPackets, float tranBytes) {
        this.recPackets = recPackets;
        this.recBytes = recBytes;
        this.tranPackets = tranPackets;
        this.tranBytes = tranBytes;
    }

    public NetworkData(float packets, float bytes) {
        this.packets = packets;
        this.bytes = bytes;
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

    public float getRecPackets() {
        return recPackets;
    }

    public void setRecPackets(float recPackets) {
        this.recPackets = recPackets;
    }

    public float getRecBytes() {
        return recBytes;
    }

    public void setRecBytes(float recBytes) {
        this.recBytes = recBytes;
    }

    public float getTranPackets() {
        return tranPackets;
    }

    public void setTranPackets(float tranPackets) {
        this.tranPackets = tranPackets;
    }

    public float getTranBytes() {
        return tranBytes;
    }

    public void setTranBytes(float tranBytes) {
        this.tranBytes = tranBytes;
    }
}
