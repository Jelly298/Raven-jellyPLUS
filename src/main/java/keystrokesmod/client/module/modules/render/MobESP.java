package keystrokesmod.client.module.modules.render;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class MobESP extends Module {
    Color MOB_COLOUR = new Color(255, 255, 0);
    Color SPECIAL_MOB_COLOUR = new Color(0, 255, 0);
    Color CHICKEN_COLOUR = new Color(255, 161, 0);


    public MobESP() {
        super("MobESP", ModuleCategory.render);
    }

    @Override
    public void onEnable() {
        for(Entity o : mc.theWorld.loadedEntityList) {
            if(o instanceof EntityLivingBase) {

                if(o instanceof EntityCreeper && !o.isInvisible()) {
                    EntityLivingBase creeper = (EntityLivingBase)o;
                    mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[MOB FINDER] "
                            + EnumChatFormatting.GREEN + "Creeper " +
                            EnumChatFormatting.WHITE + "X: " + Math.round(creeper.posX) + " Y: "+ Math.round(creeper.posY) + " Z: "+ Math.round(creeper.posZ)));
                }

                if(o instanceof EntitySlime && !o.isInvisible()) {
                    EntityLivingBase slime = (EntityLivingBase)o;
                    mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[MOB FINDER] "
                            + EnumChatFormatting.GREEN + "Slime " +
                            EnumChatFormatting.WHITE + "X: " + Math.round(slime.posX) + " Y: "+ Math.round(slime.posY) + " Z: "+ Math.round(slime.posZ)));
                }

                if(o instanceof EntityEnderman && !o.isInvisible()) {
                    EntityLivingBase enderman = (EntityLivingBase)o;
                    mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[MOB FINDER] "
                            + EnumChatFormatting.DARK_GRAY + "Enderman " +
                            EnumChatFormatting.WHITE + "X: " + Math.round(enderman.posX) + " Y: "+ Math.round(enderman.posY) + " Z: "+ Math.round(enderman.posZ)));
                }

                if(o instanceof EntityBlaze && !o.isInvisible()) {
                    EntityLivingBase blaze = (EntityLivingBase)o;
                    mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[MOB FINDER] "
                            + EnumChatFormatting.YELLOW + "Blaze " +
                            EnumChatFormatting.WHITE + "X: " + Math.round(blaze.posX) + " Y: "+ Math.round(blaze.posY) + " Z: "+ Math.round(blaze.posZ)));
                }
            }
        }

    }

    @Override
    public String getName() {
        return "MobFinder";
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        for(Entity o : mc.theWorld.loadedEntityList) {
            if(((o instanceof EntitySlime) || (o instanceof EntityMob)) && !o.isInvisible()) {
                if(o instanceof EntityCreeper || o instanceof EntityEnderman || o instanceof EntityBlaze || o instanceof EntitySlime) {
                    Utils.HUD.drawBoxAroundEntity(o, 1, 0, 0, SPECIAL_MOB_COLOUR.getRGB(), true);
                } else {
                    Utils.HUD.drawBoxAroundEntity(o, 1, 0, 0, MOB_COLOUR.getRGB(), true);
                }
            }
            if((o instanceof EntityChicken)) {
                Utils.HUD.drawBoxAroundEntity(o, 1, 0, 0, CHICKEN_COLOUR.getRGB(), true);
            }
        }
    }

}
