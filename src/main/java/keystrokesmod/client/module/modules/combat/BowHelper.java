package keystrokesmod.client.module.modules.combat;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.utils.GeneralCoord;
import keystrokesmod.client.utils.RenderUtils;
import keystrokesmod.client.utils.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BowHelper extends Module {

    static Field itemInUseTickField;

    static {
        try {
            itemInUseTickField = EntityPlayer.class.getDeclaredField("field_71072_f");// itemInUseCount
            itemInUseTickField.setAccessible(true);
        }catch (Exception ignored){
            try {
                itemInUseTickField = EntityPlayer.class.getDeclaredField("itemInUseCount");
                itemInUseTickField.setAccessible(true);
            }catch (Exception ignored2){}
        }
    }

    Entity entityHit;

    Entity entityTarget;
    AxisAlignedBB entityTargetPredictedBB;

    BlockPos blockPosHit;

    List<GeneralCoord> trajectory = new ArrayList<>();
    int simulatedTickCount;

    boolean hitPredictedTarget;

    public BowHelper() {
        super("BowHelper", ModuleCategory.combat);
    }


    @Override
    public String getName() {
        return "BowHelper";
    }

    @Override
    public void onEnable() {

        blockPosHit = null;
        entityTarget = null;
        entityTargetPredictedBB = null;
        simulatedTickCount = 0;
        hitPredictedTarget = false;
    }


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) throws IllegalAccessException {
        if(mc.thePlayer == null || mc.theWorld == null || event.phase == TickEvent.Phase.END)
            return;

        if(mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBow) {

            int itemTick = (Integer) itemInUseTickField.get(mc.thePlayer);
            simulate(itemTick);


            if(entityTargetPredictedBB != null && itemTick > 0) {
                double n = ((double) (mc.thePlayer.rotationYaw -
                        Utils.Player.fovToPoint((entityTargetPredictedBB.maxX + entityTargetPredictedBB.minX)/2d, (entityTargetPredictedBB.maxZ + entityTargetPredictedBB.minZ)/2d)) % 360.0D + 540.0D) % 360.0D - 180.0D;

                if (n > 0.01D || n < -0.01D) {
                    double complimentSpeed;
                    if(n >= 0)
                        complimentSpeed = Math.min(20, n) * (ThreadLocalRandom.current().nextDouble(25 - 1.47328, 25 + 2.48293) / 100);
                    else
                        complimentSpeed = Math.max(-20, n) * (ThreadLocalRandom.current().nextDouble(25 - 1.47328, 25 + 2.48293) / 100);

                    float val = (float) (-(complimentSpeed + n / (101.0D - (float) ThreadLocalRandom.current().nextDouble(25 - 4.723847, 25))));
                    mc.thePlayer.rotationYaw += val;
                }
            }

        } else {
            trajectory.clear();
            entityTarget = null;
            entityHit = null;
            blockPosHit = null;
        }


    }


    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if(mc.thePlayer == null || mc.theWorld == null)
            return;

        if(entityHit != null && !hitPredictedTarget)
            RenderUtils.drawEntity(entityHit, new Color(0, 0, 140, 255), 3, event.partialTicks);
        if(blockPosHit != null)
            RenderUtils.drawBlockBox(blockPosHit, new Color(0, 0, 140, 150), event.partialTicks);


        int ping = 0;
        if(mc.getNetHandler() != null && mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()) != null)
            ping = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime();
        if(entityTarget != null){
            entityTargetPredictedBB = entityTarget.getEntityBoundingBox().offset(
                    -(entityTarget.lastTickPosX - entityTarget.posX) * ((ping)/50f + simulatedTickCount),
                    0,
                    -(entityTarget.lastTickPosZ - entityTarget.posZ) * ((ping)/50f + simulatedTickCount));
            RenderUtils.drawBlockBox(entityTargetPredictedBB, hitPredictedTarget ? new Color(255, 255, 0, 255) : new Color(0, 255, 0, 100),  3);
        }

        if(!trajectory.isEmpty()){
            final Entity render = mc.getRenderViewEntity();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer bufferBuilder = tessellator.getWorldRenderer();
            final double realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * event.partialTicks;
            final double realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * event.partialTicks;
            final double realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * event.partialTicks;
            GlStateManager.pushMatrix();
            GlStateManager.translate(-realX, -realY, -realZ);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GL11.glDisable(3553);
            GL11.glLineWidth(3f);
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1f, 1f, 1f, 1f);
            bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

            for (int i = 0; i < trajectory.size() - 1; i++) {

                GeneralCoord pos = trajectory.get(i);
                GeneralCoord nextPos = trajectory.get(i + 1);
                RenderUtils.drawLine(bufferBuilder, pos, nextPos, 0, 255, 0, 200);
            }

            GeneralCoord pos = trajectory.get(0);
            GeneralCoord nextPos = trajectory.get(trajectory.size() - 1);
            RenderUtils.drawLine(bufferBuilder, pos, nextPos, 0, 255, 0, 200);

            tessellator.draw();
            GlStateManager.translate(realX, realY, realZ);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }



    }


    private void simulate(int itemInUseTick) {

        simulatedTickCount = 0;
        hitPredictedTarget = false;

        trajectory.clear();

        float actualCharge = (float)itemInUseTick / 20.0F;
        actualCharge = (actualCharge * actualCharge + actualCharge * 2.0F) / 3.0F;

        if ((double)actualCharge < 0.1D)
        {
            entityHit = null;
            blockPosHit = null;
            entityTarget = null;
            return;
        }

        if (actualCharge > 1.0F)
        {
            actualCharge = 1.0F;
        }


        double posX, posY, posZ;
        posX = mc.thePlayer.posX;
        posY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
        posZ = mc.thePlayer.posZ;
        float rotationYaw, rotationPitch;
        rotationYaw = mc.thePlayer.rotationYaw;
        rotationPitch = mc.thePlayer.rotationPitch;

        double motionX = (-MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI));
        double motionZ = (MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI));
        double motionY = (-MathHelper.sin(rotationPitch / 180.0F * (float) Math.PI));

        float g = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
        double x = motionX / (double)g;
        double y = motionY / (double)g;
        double z = motionZ / (double)g;

        double velocity = actualCharge * 2 * 1.5f; // charge
        x = x * velocity;
        y = y * velocity;
        z = z * velocity;

        motionX = x;
        motionY = y;
        motionZ = z;

        float w = 0.5f / 2.0F; // width = 0.5f
        float h = 0.5f; // height = 0.5f

        AxisAlignedBB bb;

        outerloop:
        while (true) {
            simulatedTickCount ++;

            if(simulatedTickCount > 10000)
                break;

            float f4 = 0.99F;
            float f6 = 0.05F;


            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            motionX *= f4;
            motionY *= f4;
            motionZ *= f4;
            motionY -= f6;


            trajectory.add(new GeneralCoord(posX, posY, posZ));
            bb = new AxisAlignedBB(posX - (double) w, posY, posZ - (double) w, posX + (double) w, posY + (double) h, posZ + (double) w);

            Vec3 vec31 = new Vec3(posX, posY, posZ);
            Vec3 vec3 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);

            List<Entity> entityList = new ArrayList<>();
            getEntitiesWithinAABBForEntity(bb.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D), entityList);

            float g1 = 0.3F;

            if(entityTargetPredictedBB != null) {
                MovingObjectPosition movingObjectPosition2 = entityTargetPredictedBB.expand(g1, g1, g1).calculateIntercept(vec31, vec3);
                if (movingObjectPosition2 != null) {
                    hitPredictedTarget = true;
                    blockPosHit = null;
                }
            }

            for (Entity entity : entityList) {

                if (entity.canBeCollidedWith() && (entity != mc.thePlayer))
                {

                    MovingObjectPosition movingobjectposition1 = entity.getEntityBoundingBox().expand(g1, g1, g1).calculateIntercept(vec31, vec3);

                    if (movingobjectposition1 != null) {
                        blockPosHit = null;
                        entityTarget = entity;
                        entityHit = entity;
                        break outerloop;
                    }
                }
            }



            MovingObjectPosition movingobjectposition = mc.theWorld.rayTraceBlocks(vec31, vec3, false, true, false);

            if (movingobjectposition != null) {
                blockPosHit = movingobjectposition.getBlockPos();
                //entityTarget = mc.theWorld.getClosestPlayer(blockPosHit.getX(), blockPosHit.getY(), blockPosHit.getZ(), 20);
                entityTarget = getClosetEntity(blockPosHit);
                entityHit = null;
                break;
            }
        }
    }

    public void getEntitiesWithinAABBForEntity(AxisAlignedBB aabb, List<Entity> listToFill)
    {
        for (Entity entity : mc.theWorld.getLoadedEntityList())
        {
            if (entity != mc.thePlayer && entity.getEntityBoundingBox().intersectsWith(aabb))
            {
                listToFill.add(entity);
            }
        }
    }

    public Entity getClosetEntity(BlockPos blockPos){
        List<Entity> entityList = mc.theWorld.getLoadedEntityList();
        double d0 = 999;
        Entity entity = null;
        for (Entity e : entityList) {
            // may remove this in testings
            if(!(e instanceof EntityPlayer) || e.isInvisible())
                continue;
            if(blockPos.distanceSq(e.posX, e.posY, e.posZ) < d0){
                entity = e;
                d0 = blockPos.distanceSq(e.posX, e.posY, e.posZ);
            }
        }
        return entity;
    }


}

