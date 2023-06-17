package keystrokesmod.client.mixins;

import keystrokesmod.client.main.Raven;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderChunk.class)
public class MixinRenderChunk {
    @Redirect(method = "rebuildChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/VisGraph;computeVisibility()Lnet/minecraft/client/renderer/chunk/SetVisibility;"))
    private SetVisibility tweakVisibility(VisGraph instance) {
        if (Raven.moduleManager.getModuleByName("Xray").isEnabled()) {
            if (((MixinVisGraph) instance).getField_178611_f() < 4096) {
                ((MixinVisGraph) instance).setField_178611_f(4090);
            }
        }
        return instance.computeVisibility();
    }
}
