package com.dori.SpringStory.scripts.npcs;

import com.dori.SpringStory.client.character.MapleChar;
import com.dori.SpringStory.dataHandlers.CharacterCosmeticsDataHandler;
import com.dori.SpringStory.scripts.api.MenuOption;
import com.dori.SpringStory.scripts.api.NpcScript;
import com.dori.SpringStory.scripts.api.ScriptApi;
import com.dori.SpringStory.utils.NpcMessageUtils;
import com.dori.SpringStory.utils.NpcScriptUtils;

import java.util.*;

public class GlobalScripts {

    // Regular Cab in Victoria
    // Text for this script is 100% GMS-like
    @NpcScript(id = 1012000)
    public static ScriptApi handleTaxi(MapleChar chr) {
        ScriptApi script = new ScriptApi();
        String npcName = NpcMessageUtils.npcName(1012000);
        script.sayNext("Hello! I'm ")
                .blue(npcName)
                .addMsg(", and I am here to take you to your destination quickly and safely.")
                .blue(npcName)
                .addMsg(" values your satisfaction, so you can always reach your destination at an affordable price.")
                .addMsg(" I am here to serve you.");

        // Generate a list of destinations the player can go to
        List<Integer> taxiMaps = Arrays.asList(100000000, 101000000, 102000000, 103000000, 104000000, 105000000, 120000000);
        List<MenuOption> menuOptions = new ArrayList<>();
        for (Integer mapId : taxiMaps) {
            if (chr.getMapId() != mapId) {
                // Once big bang hit, all taxi options cost 1,000 meso
                menuOptions.add(NpcScriptUtils.addTaxiMoveOption(script, chr, mapId, chr.getJob() == 0, 1000));
            }
        }

        script.askMenu("Please select your destination.\r\n", menuOptions);
        return script;
    }

    private static List<Integer> getListOfColoredStyles(int cosmeticID) {
        List<Integer> listOfAllColors = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            listOfAllColors.add(cosmeticID + i);
        }
        return listOfAllColors;
    }

    @NpcScript(id = 9900001)
    public static ScriptApi handleNimaKIN(MapleChar chr) {
        ScriptApi script = new ScriptApi();
        script.sayNext("Hello! I'm NimaKIN, and i dream to be a ").blue("stylist ").addMsg(":D")
                .askMenu("What do you want to do today?",
                        script.addMenuOption("Change your hairstyle", () -> script.askAvatarHair("Choose new hair -", CharacterCosmeticsDataHandler.getAllUniqueHairs())),
                        script.addMenuOption("Change your hairstyle color", () -> script.askAvatarHair("Choose new hair color -", getListOfColoredStyles(chr.getHair()))),
                        script.addMenuOption("Change your eyes", () -> script.askAvatarFace("Choose new eyes -", CharacterCosmeticsDataHandler.getAllFaces())),
                        script.addMenuOption("Change your eyes color", () -> script.askAvatarFace("Choose new eyes color -", getListOfColoredStyles(chr.getFace()))),
                        script.addMenuOption("Change your skin color", () -> script.askAvatarSkin("Choose new skin -", List.of(0, 1, 2, 3, 4)))
                        );
        ;
        return script;
    }
}
