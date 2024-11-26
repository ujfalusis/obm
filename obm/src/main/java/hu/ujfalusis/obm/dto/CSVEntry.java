package hu.ujfalusis.obm.dto;

import java.time.LocalDateTime;

public class CSVEntry {
    
    private LocalDateTime serverTime;
    private float bestBidPrice;
    private float besAskPrice;
    private float avgBuyPrice;
    private float avgSellPrice;
    
    public CSVEntry(LocalDateTime serverTime, float bestBidPrice, float besAskPrice, float avgBuyPrice, float avgSellPrice) {
        this.serverTime = serverTime;
        this.bestBidPrice = bestBidPrice;
        this.besAskPrice = besAskPrice;
        this.avgBuyPrice = avgBuyPrice;
        this.avgSellPrice = avgSellPrice;
    }
    public LocalDateTime getServerTime() {
        return serverTime;
    }
    public void setServerTime(LocalDateTime serverTime) {
        this.serverTime = serverTime;
    }
    public float getBestBidPrice() {
        return bestBidPrice;
    }
    public void setBestBidPrice(float bestBidPrice) {
        this.bestBidPrice = bestBidPrice;
    }
    public float getBesAskPrice() {
        return besAskPrice;
    }
    public void setBesAskPrice(float besAskPrice) {
        this.besAskPrice = besAskPrice;
    }
    public float getAvgBuyPrice() {
        return avgBuyPrice;
    }
    public void setAvgBuyPrice(float avgBuyPrice) {
        this.avgBuyPrice = avgBuyPrice;
    }
    public float getAvgSellPrice() {
        return avgSellPrice;
    }
    public void setAvgSellPrice(float avgSellPrice) {
        this.avgSellPrice = avgSellPrice;
    }
}
