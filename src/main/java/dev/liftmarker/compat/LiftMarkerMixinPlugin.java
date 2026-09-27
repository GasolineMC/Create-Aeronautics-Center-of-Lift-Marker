package dev.liftmarker.compat;

import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

/**
 * Skips every diagram mixin together when the installed Simulated is missing something they need, so an
 * unknown version loads without the marker instead of failing to start
 */
public class LiftMarkerMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Lift Marker");

    private Boolean compatible = null;

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        if (this.compatible == null) {
            final List<String> missing = SimulatedCompatibility.findMissing(
                    name -> MixinService.getService().getBytecodeProvider().getClassNode(name.replace('/', '.')));
            this.compatible = missing.isEmpty();

            if (!this.compatible) {
                LOGGER.warn("This version of Simulated is not supported, so the Center of Lift marker is turned off. Missing: {}", missing);
            }
        }

        return this.compatible;
    }

    @Override
    public void onLoad(final String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
    }
}
