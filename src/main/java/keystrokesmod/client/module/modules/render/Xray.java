package keystrokesmod.client.module.modules.render;


import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.EvictingList;
import keystrokesmod.client.utils.RenderUtils;
import keystrokesmod.client.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


import java.awt.*;
import java.util.ArrayList;


public final class Xray extends Module {

   Minecraft mc = Minecraft.getMinecraft();
   public static ArrayList<Block> targetBlocks = null;
   public static EvictingList<BlockPos> interactedBlocks = new EvictingList<>(2000);

   public static SliderSetting opacity;

   public static TickSetting ESPsc;

   public final static Color SC_COLOR = new Color(0, 255, 0);
   public final static Color YF_COLOR = new Color(255, 255, 0);
   public final static Color RF_COLOR = new Color(255, 0, 0);

   public Xray() {
      super("Xray", ModuleCategory.render);
      this.registerSetting(opacity = new SliderSetting("Opacity", 0.0D, 0.0D, 100.0D, 1.0D));
      this.registerSetting(ESPsc = new TickSetting("ESP Sugarcane", false));
   }


   @Override
   public void onEnable() {
      targetBlocks = new ArrayList<Block>() {{
         add(Blocks.diamond_ore);
         add(Blocks.emerald_ore);
         add(Blocks.iron_ore);
         add(Blocks.gold_ore);
         add(Blocks.coal_ore);
         add(Blocks.lava);
         add(Blocks.redstone_ore);
         add(Blocks.lit_redstone_ore);
      }};

      mc.renderGlobal.loadRenderers();
      interactedBlocks.clear();
   }


   @Override
   public void onDisable() {
      targetBlocks = null;
      interactedBlocks.clear();
      mc.renderGlobal.loadRenderers();
   }

   @SubscribeEvent
   public void onRender(RenderWorldLastEvent event) {
      if(!ESPsc.isToggled())
         return;

      for(int y = 0; y < 5; y++) {
         for(int x = -120; x < 120; x++) {
            for(int z = -120; z < 120; z++) {

               BlockPos checkBlock = new BlockPos(mc.thePlayer.posX + x, 63 + y, mc.thePlayer.posZ + z);
               IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(checkBlock);
               if(blockState != null && blockState.getBlock() != null) {
                  if(blockState.getBlock().equals(Blocks.reeds)){
                     RenderUtils.drawBlockBox(checkBlock, SC_COLOR, 1f);
                  }
                  if(blockState.getBlock().equals(Blocks.red_flower)) {
                     RenderUtils.drawBlockBox(checkBlock, RF_COLOR, 1f);
                  }
                  if(blockState.getBlock().equals(Blocks.yellow_flower)) {
                     RenderUtils.drawBlockBox(checkBlock, YF_COLOR, 1f);
                  }
               }

            }
         }
      }

   }


   // used in replacements/BlockRender
   public static int getBlockAlpha (IBlockState state){
      if(!targetBlocks.contains(state.getBlock())) {
         return (int) opacity.getInput();
      }
      return -1;
   }

   public static boolean modifyRenderSideHook(Block block, boolean returnValue) {
      if(!returnValue && !Xray.targetBlocks.contains(block)) {
         return true;
      }
      return returnValue;
   }
}
