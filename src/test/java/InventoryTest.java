import com.vitaxa.jasteambot.steam.GameType;
import com.vitaxa.jasteambot.steam.inventory.Inventory;
import com.vitaxa.jasteambot.steam.inventory.strategy.InventoryLoader;
import com.vitaxa.steamauth.helper.Json;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.when;

// TODO: Improve tests
@RunWith(MockitoJUnitRunner.Silent.class)
public class InventoryTest {

    @Mock
    private InventoryLoader inventoryLoader;

    @Test
    public void CSGOInventoryLoader() {
        when(inventoryLoader.loadInventory())
                .thenReturn(Json.getInstance().fromJson(TestResponses.gameInventory(GameType.CSGO), Inventory.class));
        Assert.assertThat(inventoryLoader.loadInventory(), notNullValue());
    }

    @Test
    public void DOTAInventoryLoader() {
        when(inventoryLoader.loadInventory())
                .thenReturn(Json.getInstance().fromJson(TestResponses.gameInventory(GameType.DOTA2), Inventory.class));
        Assert.assertThat(inventoryLoader.loadInventory(), notNullValue());
    }

    @Test
    public void PUBGInventoryLoader() {
        when(inventoryLoader.loadInventory())
                .thenReturn(Json.getInstance().fromJson(TestResponses.gameInventory(GameType.PUBG), Inventory.class));
        Assert.assertThat(inventoryLoader.loadInventory(), notNullValue());
    }


}
