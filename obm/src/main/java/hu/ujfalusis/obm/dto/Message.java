package hu.ujfalusis.obm.dto;
public class Message<T> {

    private String topic;
    private String type;
    private long ts;
    private T data;
    private long cts;

    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public long getTs() {
        return ts;
    }
    public void setTs(long ts) {
        this.ts = ts;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
    public long getCts() {
        return cts;
    }
    public void setCts(long cts) {
        this.cts = cts;
    }
}
