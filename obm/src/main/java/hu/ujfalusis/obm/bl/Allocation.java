package hu.ujfalusis.obm.bl;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Allocation {
    
    private static final Logger logger = LoggerFactory.getLogger(Allocation.class);

    private final List<Float> amounts;
    private final List<Float> prices;
    private Float missingValue;

    public Allocation(final float totalValue) {
        amounts = new ArrayList<>();
        prices = new ArrayList<>();
        this.missingValue = totalValue;
    }

    public boolean allocate(float amount, float price) {
        final var availableValue = amount * price;
        if (availableValue <= this.missingValue) {
            amounts.add(amount);
            prices.add(price);
            this.missingValue -= availableValue;
        } else {
            final var missingAmount = this.missingValue / price;
            amounts.add(missingAmount);
            prices.add(price);
            this.missingValue = 0f;
        }
        logger.debug("amounts: {}, prices: {}, missingValue: {}.", amounts, prices, this.missingValue);
        return missingValue == 0f;
    }

    public float avarage() {
        var totalValue = 0f;
        var totalAmount = 0f;
        for (var i = 0; i < amounts.size(); i++) {
            final var amount = amounts.get(i);
            totalValue += amount * prices.get(i);
            totalAmount += amount;
        }
        return totalValue / totalAmount;
    }
}
