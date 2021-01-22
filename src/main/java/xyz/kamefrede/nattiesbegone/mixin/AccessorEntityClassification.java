package xyz.kamefrede.nattiesbegone.mixin;

import net.minecraft.entity.EntityClassification;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(EntityClassification.class)
public interface AccessorEntityClassification {
	@Accessor
	static Map<String, EntityClassification> getVALUES_MAP() {
		throw new AssertionError();
	}
}
