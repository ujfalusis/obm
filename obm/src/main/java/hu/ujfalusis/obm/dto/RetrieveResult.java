package hu.ujfalusis.obm.dto;

public class RetrieveResult {

    private final float avaragePrice;
    private final float best;

    public RetrieveResult(float avaragePrice, float best) {
        this.avaragePrice = avaragePrice;
        this.best = best;
    }
    public float getAvaragePrice() {
        return avaragePrice;
    }
    public float getBest() {
        return best;
    }
}
