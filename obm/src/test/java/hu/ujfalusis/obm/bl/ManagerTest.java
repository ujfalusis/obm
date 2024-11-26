package hu.ujfalusis.obm.bl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import hu.ujfalusis.obm.dto.OrderbookData;
import hu.ujfalusis.obm.dto.OrderbookSide;
import hu.ujfalusis.obm.dto.RetrieveResult;

public class ManagerTest {
    
    private static float FLOATING_DIFF_LIMIT = 1e-9f;  
    private Manager manager;

    @Before
    public void setUp() {
        manager = new Manager();
    }

    @Test
    public void testProcess() {
        OrderbookData dat = new OrderbookData();
        dat.setA(List.of(List.of(9f, 4f), List.of(8f, 6f)));
        dat.setB(List.of(List.of(11f, 5f), List.of(12f, 3f)));
        dat.setSeq(1l);
        manager.process(dat, "snapshot");

        RetrieveResult retrieve = manager.retrieve(60, OrderbookSide.ASKS, dat.getSeq());
        assertEquals(8f, retrieve.getBest(), 0);
        assertEquals((6f * 8f + 12f/9 * 9f) / (6f + 12/9f), retrieve.getAvaragePrice(), FLOATING_DIFF_LIMIT);
        retrieve = manager.retrieve(60, OrderbookSide.BIDS, dat.getSeq());
        assertEquals(12, retrieve.getBest(), 0);
        assertEquals((3f * 12f + 24f/11f * 11f) / (3f + 24f/11f), retrieve.getAvaragePrice(), FLOATING_DIFF_LIMIT);
    }

    @Test
    public void testProcessWithUpdate() {
        OrderbookData dat = new OrderbookData();
        dat.setA(List.of(List.of(9f, 4f), List.of(8f, 6f)));
        dat.setB(List.of(List.of(11f, 5f), List.of(12f, 3f)));
        dat.setSeq(1l);
        // place snapshot
        manager.process(dat, "snapshot");

        OrderbookData datu = new OrderbookData();
        datu.setA(List.of(List.of(10f, 3f)));
        datu.setB(List.of(List.of(10.5f, 2f)));
        datu.setSeq(2l);
        datu.setU(2);
        // place update
        manager.process(datu, "update");

        // retrieve by snapshot seq
        // RetrieveResult retrieve = manager.retrieve(60, OrderbookSide.ASKS, dat.getSeq());
        // assertEquals(9, retrieve.getBest(), 0);
        // assertEquals((4f * 9f + 3f * 8f) / (4f + 3f), retrieve.getAvaragePrice(), FLOATING_DIFF_LIMIT);
        
        // retrieve = manager.retrieve(60, OrderbookSide.BIDS, dat.getSeq());
        // assertEquals(11, retrieve.getBest(), 0);
        // assertEquals((5f * 11f + 5f/12f * 12f) / (5f + 5f/12f), retrieve.getAvaragePrice(), FLOATING_DIFF_LIMIT);

        // retrieve by update seq
        RetrieveResult retrieveu = manager.retrieve(40, OrderbookSide.ASKS, datu.getSeq());
        assertEquals(8, retrieveu.getBest(), 0);
        // 10, 3 -> 9, 4 -> ...
        assertEquals((6/1.2f * 8f) / (6/1.2f), retrieveu.getAvaragePrice(), FLOATING_DIFF_LIMIT);
        
        retrieveu = manager.retrieve(40, OrderbookSide.BIDS, datu.getSeq());
        assertEquals(12f, retrieveu.getBest(), 0);
        // 10.5, 2 -> 11, 5 -> ...
        assertEquals((3f * 12f + 4f/11f * 11f) / (3f + 4f/11f), retrieveu.getAvaragePrice(), FLOATING_DIFF_LIMIT);
    }
}
