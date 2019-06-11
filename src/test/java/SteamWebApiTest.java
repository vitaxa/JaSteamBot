import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.BotConfig;
import com.vitaxa.jasteambot.steam.GameType;
import com.vitaxa.jasteambot.steam.web.SteamWeb;
import com.vitaxa.jasteambot.steam.web.SteamWebApi;
import com.vitaxa.jasteambot.steam.web.model.ItemAssetInfo;
import com.vitaxa.jasteambot.steam.web.model.Match;
import com.vitaxa.jasteambot.steam.web.model.SteamProfileInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// TODO: Improve tests
@RunWith(MockitoJUnitRunner.Silent.class)
public class SteamWebApiTest {

    @Mock
    private Bot bot;

    @Mock
    private BotConfig botConfig;

    @Mock
    private SteamWeb steamWeb;

    @Before
    public void setUp() {
        when(bot.getBotConfig()).thenReturn(botConfig);
        when(bot.getSteamWeb()).thenReturn(steamWeb);
    }

    @Test
    public void CSGOItemInfo() {
        when(steamWeb.fetch(anyString(), any())).thenReturn(TestResponses.assetClassInfo(GameType.CSGO));
        final SteamWebApi steamWebApi = new SteamWebApi(bot);
        final Optional<ItemAssetInfo> itemInfoOptional = steamWebApi.getItemInfo(GameType.CSGO, "123456789");
        assertTrue(itemInfoOptional.isPresent());
    }

    @Test
    public void DOTAItemInfo() {
        when(steamWeb.fetch(anyString(), any())).thenReturn(TestResponses.assetClassInfo(GameType.DOTA2));
        final SteamWebApi steamWebApi = new SteamWebApi(bot);
        final Optional<ItemAssetInfo> itemInfoOptional = steamWebApi.getItemInfo(GameType.DOTA2, "123456789");
        assertTrue(itemInfoOptional.isPresent());
    }

    @Test
    public void PUBGItemInfo() {
        when(steamWeb.fetch(anyString(), any())).thenReturn(TestResponses.assetClassInfo(GameType.PUBG));
        final SteamWebApi steamWebApi = new SteamWebApi(bot);
        final Optional<ItemAssetInfo> itemInfoOptional = steamWebApi.getItemInfo(GameType.PUBG, "123456789");
        assertTrue(itemInfoOptional.isPresent());
    }

    @Test
    public void profileInfo() {
        when(steamWeb.fetch(anyString(), any())).thenReturn(TestResponses.profileInfo());
        final SteamWebApi steamWebApi = new SteamWebApi(bot);
        final Optional<SteamProfileInfo> profileInfoOptional = steamWebApi.getProfileInfo("123456789");
        assertTrue(profileInfoOptional.isPresent());
    }

    @Test
    public void dota2MatchHistory() {
        when(steamWeb.fetch(anyString(), any())).thenReturn(TestResponses.dotaMatchHistory());
        final SteamWebApi steamWebApi = new SteamWebApi(bot);
        final List<Match> matchHistory = steamWebApi.getDotaMatchHistory(20);
        assertTrue(matchHistory != null && !matchHistory.isEmpty());
    }

}
