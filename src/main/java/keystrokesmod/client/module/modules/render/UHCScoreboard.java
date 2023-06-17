package keystrokesmod.client.module.modules.render;

import keystrokesmod.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.stream.Collectors;

public class UHCScoreboard extends Module {
    Minecraft mc = Minecraft.getMinecraft();
    public static HashMap<String, Integer> score = new HashMap<>();

    static final String header = "--Top killers--";

    public UHCScoreboard() {
        super("UHC Scoreboard", ModuleCategory.render);
    }

    @SubscribeEvent
    public void onOverlayRender(RenderGameOverlayEvent event){
        if(event.type == RenderGameOverlayEvent.ElementType.TEXT){

            mc.fontRendererObj.drawStringWithShadow(
                    header,
                    event.resolution.getScaledWidth() - mc.fontRendererObj.getStringWidth(header) - 5,
                    event.resolution.getScaledHeight() - 60,
                    -1);


            if(score.isEmpty()){
                mc.fontRendererObj.drawStringWithShadow(
                        "N/A",
                        event.resolution.getScaledWidth() - mc.fontRendererObj.getStringWidth("N/A") - 5,
                        event.resolution.getScaledHeight() - 48,
                        -1);
                return;
            }

            int count = 0;
            for(String playerName : score.keySet()) {

                if(count >= 36) break;

                String text = playerName + ": " + score.get(playerName) + " kills";
                mc.fontRendererObj.drawStringWithShadow(
                        text,
                        event.resolution.getScaledWidth() - mc.fontRendererObj.getStringWidth(text) - 5,
                        event.resolution.getScaledHeight() - 48 + count,
                        -1
                );
                count += 12;



            }

        }
    }
    @SubscribeEvent
    public void onChatReceivedMessage(ClientChatReceivedEvent event){


        String message = event.message.getUnformattedText();
        if(message.contains("UHC Champions"))
            score.clear(); // started a new game

        if(!getScoreboardDisplayName(1).contains("UHC"))
            return;

        if(message.contains("was slain")){
            String temp = message.substring(message.indexOf("by") + 3);
            String killerName = temp.substring(0, temp.indexOf(" "));
            if(score.containsKey(killerName))
                score.computeIfPresent(killerName, (k, v) -> v + 1);
            else
                score.put(killerName, 1);

            score = score.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1, LinkedHashMap::new));
        }
    }

    public static String getScoreboardDisplayName(int line) {
        try {
            return Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(line).getDisplayName();
        } catch (Exception e) {
            System.out.println("Error in getting scoreboard " + e);
            return "";
        }
    }
}
