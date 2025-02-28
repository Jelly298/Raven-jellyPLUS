package keystrokesmod.client.mixins;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.modules.render.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Block.class)
public abstract class MixinBlock {
    private final Block block = (Block) (Object) this;

    @Shadow
    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {}

    @Shadow
    public abstract void setBlockBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);

    @Inject(method = "shouldSideBeRendered", at = @At("RETURN"), cancellable = true)
    private void onShouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        if(Raven.moduleManager.getModuleByName("Xray").isEnabled()) {
            cir.setReturnValue(Xray.modifyRenderSideHook(block, cir.getReturnValue()));
        }
    }

    @Inject(method = "canRenderInLayer", at = @At("HEAD"), cancellable = true, remap = false)
    private void tweakCanRender(EnumWorldBlockLayer layer, CallbackInfoReturnable<Boolean> cir){
        if (Raven.moduleManager.getModuleByName("Xray").isEnabled()) {
            cir.setReturnValue(EnumWorldBlockLayer.TRANSLUCENT == layer);
        }
    }


}
