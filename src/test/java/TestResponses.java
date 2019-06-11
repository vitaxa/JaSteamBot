import com.vitaxa.jasteambot.helper.IOHelper;
import com.vitaxa.jasteambot.steam.GameType;

import java.io.IOException;

public class TestResponses {

    public static String gameInventory(GameType gameType) {
        final String fileName;
        switch (gameType) {
            case CSGO:
                fileName = "inv/csgo.inv";
                break;
            case DOTA2:
                fileName = "inv/dota.inv";
                break;
            case PUBG:
                fileName = "inv/pubg.inv";
                break;
            default:
                throw new AssertionError("Unknown gameType" + gameType.name());
        }
        return IOHelper.decode(loadFile(fileName));
    }

    public static String assetClassInfo(GameType gameType) {
        final String fileName;
        switch (gameType) {
            case CSGO:
                fileName = "webapi/csgo_items.info";
                break;
            case DOTA2:
                fileName = "webapi/dota_items.info";
                break;
            case PUBG:
                fileName = "webapi/pubg_items.info";
                break;
            default:
                throw new AssertionError("Unknown gameType" + gameType.name());
        }
        return IOHelper.decode(loadFile(fileName));
    }

    public static String profileInfo() {
        return IOHelper.decode(loadFile("webapi/profile.info"));
    }

    public static String dotaMatchHistory() {
        return IOHelper.decode(loadFile("webapi/dota_match_history.info"));
    }

    private static byte[] loadFile(String fileName) {
        try {
            return IOHelper.read(TestResponses.class.getClassLoader().getResourceAsStream(fileName));
        } catch (IOException e) {
            return null;
        }
    }

}
