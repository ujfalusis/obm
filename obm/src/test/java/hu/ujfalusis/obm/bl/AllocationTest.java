package hu.ujfalusis.obm.bl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class AllocationTest {

    private Allocation allocation;

    @Before
    public void setUp() {
        allocation = new Allocation(100);
    }

    @Test
    public void testAllocate() {
        assertFalse(allocation.allocate(10, 9));
        assert(allocation.allocate(1, 10));
        assert(allocation.allocate(1, 1));
    }

    @Test
    public void testAvarage() {
        allocation.allocate(5, 10);
        assertEquals(10, allocation.avarage(), 0f);
        allocation.allocate(5, 5);
        assertEquals(7.5, allocation.avarage(), 0f);
        allocation.allocate(5, 10);
        assertEquals(8, allocation.avarage(), 0f);

    }
}
