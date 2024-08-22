import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import com.example.cs340final.model.InventoryItem


class InventoryItemTest {

    private lateinit var item: InventoryItem

    @Before
    fun setUp() {
        item = InventoryItem(1, "Test Item", 10)
    }

    @Test
    fun testUpdateQuantity() {
        item.updateQuantity(15)
        assertEquals(15, item.quantity)
    }

    @Test
    fun testDecreaseQuantity() {
        item.decreaseQuantity(5)
        assertEquals(5, item.quantity)
    }

    @Test
    fun testDecreaseQuantityNotBelowZero() {
        item.decreaseQuantity(15)
        assertEquals(0, item.quantity) // Assuming you adjust the method to not allow negative quantities
    }
}
