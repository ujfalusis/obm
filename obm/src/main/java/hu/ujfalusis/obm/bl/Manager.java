package hu.ujfalusis.obm.bl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.ujfalusis.obm.dto.OrderbookData;
import hu.ujfalusis.obm.dto.OrderbookSide;
import hu.ujfalusis.obm.dto.RetrieveResult;

public class Manager {

    private static final Logger logger = LoggerFactory.getLogger(Manager.class);

    // private final ConcurrentSkipListMap<Float, ConcurrentSkipListMap<Long, Float>> bids;
    // private final ConcurrentSkipListMap<Float, ConcurrentSkipListMap<Long, Float>> asks;
    // private final Map<OrderbookSide, ConcurrentSkipListMap<Float, ConcurrentSkipListMap<Long, Float>>> ob;

    private final ConcurrentSkipListMap<Float, Float> bids;
    private final ConcurrentSkipListMap<Float, Float> asks;
    private final Map<OrderbookSide, ConcurrentSkipListMap<Float, Float>> ob;

    private AtomicLong lastSequence;

    public Manager() {
        
        // ConcurrentSkipListMap entries are ordered by key, what is the price here
        // so you can easily iterate over the prices

        // bids are in ascending order by price, first is the lowest, last is the highest
        // legmagasabb ár amin vevő venni akar 
        this.bids = new ConcurrentSkipListMap<>((price1, price2) -> -1 * Float.compare(price1, price2));
        // asks are in descending order by price, first is the highest, last is the lowest 
        this.asks = new ConcurrentSkipListMap<>();

        this.ob = new ConcurrentHashMap<>(2);
        this.ob.put(OrderbookSide.ASKS, asks);
        this.ob.put(OrderbookSide.BIDS, bids);
      
        lastSequence = new AtomicLong(-1L);
    }

    public void process(final OrderbookData data, final String type) {
        final int u = data.getU();
        final long seq = data.getSeq();

        if ("snapshot" == type || 1 == u) {
            this.ob.forEach((_, v) -> v.clear());
            logger.info("Snapshot type received, order book cleared, type: {}, u: {}.", type, u);
        }

        BiConsumer<List<Float>, OrderbookSide> cons = (List<Float> e, OrderbookSide side) -> {
            final float price = e.get(0);
            final float amount = e.get(1);
            final ConcurrentSkipListMap<Float, Float> obs = ob.get(side);

            // NavigableMap<Long, Float> depth = obs.computeIfAbsent(price, (_) -> new ConcurrentSkipListMap<Long, Float>());
            // depth.put(seq, amount);
            if (amount == 0f) {
                obs.remove(price);
            } else {
                obs.put(price, amount);
            }

            logger.debug("New {} processed, price: {}, amount: {}.", side, price, amount);
        };
        data.getA().forEach((e) -> {
            cons.accept(e, OrderbookSide.ASKS);
        });
        data.getB().forEach((e) -> {
            cons.accept(e, OrderbookSide.BIDS);
        });

        // final float lastAskPrice = data.getA().getLast().get(0);
        lastSequence.set(seq);
    }

    public RetrieveResult retrieve(float totalValue, OrderbookSide side, long seq) {
        // final ConcurrentSkipListMap<Float, ConcurrentSkipListMap<Long, Float>> obs = ob.get(side);
        final ConcurrentSkipListMap<Float, Float> obs = ob.get(side);
        final Allocation allocation = new Allocation(totalValue);
        boolean allAllocated = false;
        Float bestPrice = null;
        // for (final Iterator<Entry<Float, ConcurrentSkipListMap<Long, Float>>> i = obs.entrySet().iterator(); i.hasNext() && allAllocated == false;) {
        //     final Entry<Float, ConcurrentSkipListMap<Long, Float>> depth = i.next();
        //     final Float price = depth.getKey();
        //     final ConcurrentSkipListMap<Long, Float> history = depth.getValue();
        //     final Entry<Long, Float> relevant = history.floorEntry(seq);
        //     if (relevant != null && relevant.getValue() != 0f) {
        //         final Float amount = relevant.getValue();
        //         allAllocated = allocation.allocate(amount, price);
        //         if (bestPrice == null) {
        //             bestPrice = price;
        //         }
        //     }
        // }
        for (final Iterator<Entry<Float, Float>> i = obs.entrySet().iterator(); i.hasNext() && allAllocated == false;) {
            final Entry<Float, Float> depth = i.next();
            final Float price = depth.getKey();
            final Float amount = depth.getValue();
            allAllocated = allocation.allocate(amount, price);
            if (bestPrice == null) {
                bestPrice = price;
            }
        }
        return new RetrieveResult(allocation.avarage(), bestPrice);
    }
}
