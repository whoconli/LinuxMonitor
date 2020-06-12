package Utils;

public class PerformanceStructure {
    private long time;
    private float value;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public PerformanceStructure(long time, float value) {

        this.time = time;
        this.value = value;
    }
}
