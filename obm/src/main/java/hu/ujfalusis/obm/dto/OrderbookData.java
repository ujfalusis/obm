package hu.ujfalusis.obm.dto;
import java.util.List;

public class OrderbookData {
    
    private String s;
    private List<List<Float>> b;
    private List<List<Float>> a;
    private int u;
    private long seq;

    public String getS() {
        return s;
    }
    public void setS(String s) {
        this.s = s;
    }
    public List<List<Float>> getB() {
        return b;
    }
    public void setB(List<List<Float>> b) {
        this.b = b;
    }
    public List<List<Float>> getA() {
        return a;
    }
    public void setA(List<List<Float>> a) {
        this.a = a;
    }
    public int getU() {
        return u;
    }
    public void setU(int u) {
        this.u = u;
    }
    public long getSeq() {
        return seq;
    }
    public void setSeq(long seq) {
        this.seq = seq;
    }
}
