package keystrokesmod.client.module.modules.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.utils.RenderUtils;
import keystrokesmod.client.utils.Stats;
import keystrokesmod.client.utils.StatsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Timer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UHCNametags extends Module {


    private volatile boolean statsLock = false;
    private Timer timer;
    private final HashMap<String, Stats> StatsStorage = new HashMap<>();
    private final List<String> playerChecking = new ArrayList<>();

    public UHCNametags() {
        super("UHC Nametags", ModuleCategory.render);
    }

    @Override
    public void onEnable() {
        try {
            timer = (Timer) FieldUtils.getDeclaredField(Minecraft.class, "timer", true).get(Minecraft.getMinecraft());
        }catch (Exception e){
            try {
                timer = (Timer) FieldUtils.getDeclaredField(Minecraft.class, "field_71428_T", true).get(Minecraft.getMinecraft());
            }catch (Exception e2){
                e2.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {

        for (Entity o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityPlayer) || o.isInvisible() || o.equals(mc.thePlayer))
                continue;

            String TrimmedUUID;
            try {
                TrimmedUUID = ((EntityPlayer) o).getGameProfile().getId().toString().replaceAll("-", "");
            } catch (Exception e) {
                TrimmedUUID = "-1";
            }

            if (!statsLock && StatsStorage.get(TrimmedUUID) == null) {
                if (!playerChecking.contains(TrimmedUUID)) {
                    new Thread(new getPlayerStats(TrimmedUUID)).start();
                    statsLock = true;
                    playerChecking.add(TrimmedUUID);
                }

            }

            if(StatsStorage.get(TrimmedUUID) == null)
                continue;

            try {
                double xPos = (o.lastTickPosX + (o.posX - o.lastTickPosX) * timer.renderPartialTicks) - mc.getRenderManager().viewerPosX;
                double yPos = (o.lastTickPosY + (o.posY - o.lastTickPosY) * timer.renderPartialTicks) - mc.getRenderManager().viewerPosY;
                double zPos = (o.lastTickPosZ + (o.posZ - o.lastTickPosZ) * timer.renderPartialTicks) - mc.getRenderManager().viewerPosZ;

                if (StatsStorage.get(TrimmedUUID).isNicked()) {
                    RenderUtils.renderLivingLabel(o, EnumChatFormatting.YELLOW + "Nicked Player", xPos, yPos + 1f, zPos, 200);
                } else {
                    RenderUtils.renderLivingLabel(o,
                            EnumChatFormatting.YELLOW + "[UHC K/D]: " + EnumChatFormatting.WHITE + StatsStorage.get(TrimmedUUID).getUhcKD()
                                    + EnumChatFormatting.YELLOW + " [Total kills] : " + EnumChatFormatting.WHITE + StatsStorage.get(TrimmedUUID).getUhcKills(),
                                            xPos, yPos + 1, zPos, 200);
                    RenderUtils.renderLivingLabel(o,
                                        EnumChatFormatting.YELLOW + " [Kills] : " + EnumChatFormatting.RED +
                                                (UHCScoreboard.score.containsKey(o.getName()) ? UHCScoreboard.score.get(o.getName()) : "0"),

                                    xPos, yPos + 1.5, zPos, 200);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    class getPlayerStats implements Runnable{

        String TrimmedUUID;
        public getPlayerStats(String UUID) {
            this.TrimmedUUID = UUID;
        }

        private Integer parseInt(String s){
            try {
                return Integer.parseInt(s);
            }catch (Exception ignored){}
            return 0;
        }

        private String getStringFromJson(JsonObject json, String key){
            try {
                return json.get(key).toString();
            } catch (Exception ignored){}
            return null;
        }

        private double roundTo2DecimalPlaces(double d){
            return Math.round(d * 100.0) / 100.0;
        }

        @Override
        public void run() {

            String STATS = StatsUtils.getStats(TrimmedUUID).trim();

            if(STATS.equals("403") || STATS.equals("429")) {
                mc.thePlayer.addChatMessage(new ChatComponentText("API Limit reached. Please run /api new and type in the command /i <Your APIKey>"));
                statsLock = false;
                return;
            }
            if(STATS.equals("422") || STATS.equals("{\"success\":true,\"player\":null}")) {
                StatsStorage.put(TrimmedUUID, new Stats(true));
                statsLock = false;
                return;
            }

            JsonObject STATSJSONPlayer;
            JsonObject STATSJSONstats;
            JsonObject STATSJSONUHC;

            try {
                STATSJSONPlayer = new JsonParser().parse(STATS).getAsJsonObject().get("player").getAsJsonObject();
                STATSJSONstats = STATSJSONPlayer.get("stats").getAsJsonObject();
            } catch (Exception e) {
                StatsStorage.put(TrimmedUUID, new Stats(true));
                statsLock = false;
                return;
            }

            try{
                STATSJSONUHC = STATSJSONstats.get("UHC").getAsJsonObject();
            } catch (Exception e){
                int exp = parseInt(getStringFromJson(STATSJSONPlayer, "networkExp"));
                StatsStorage.put(TrimmedUUID, new Stats(false, (int) (7 * Math.log((exp / 5000d) + 1) / Math.log(2)), 0, 0));
                statsLock = false;
                return;
            }

            try {
                int exp = parseInt(getStringFromJson(STATSJSONPlayer, "networkExp"));
                StatsStorage.put(TrimmedUUID, new Stats(
                        false,
                        ((int) (7 * Math.log((exp / 5000d) + 1) / Math.log(2))),
                        parseInt(getStringFromJson(STATSJSONUHC, "kills")) + parseInt(getStringFromJson(STATSJSONUHC, "kills_solo")),
                        roundTo2DecimalPlaces(  (parseInt(getStringFromJson(STATSJSONUHC, "kills")) + parseInt(getStringFromJson(STATSJSONUHC, "kills_solo"))) * 1.0
                                / (parseInt(getStringFromJson(STATSJSONUHC, "deaths")) + parseInt(getStringFromJson(STATSJSONUHC, "deaths_solo"))))

                ));
            } catch (Exception e) {
                StatsStorage.put(TrimmedUUID, new Stats(
                        false,
                        0,
                        parseInt(getStringFromJson(STATSJSONUHC, "kills")) + parseInt(getStringFromJson(STATSJSONUHC, "kills_solo")),
                        (parseInt(getStringFromJson(STATSJSONUHC, "kills")) + parseInt(getStringFromJson(STATSJSONUHC, "kills_solo")))
                ));
            }
            statsLock = false;

        }

    }
}
