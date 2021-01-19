package xyz.kamefrede.nattiesbegone.mixin;

import net.minecraft.entity.EntityClassification;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(EntityClassification.class)
public interface AccessorEntityClassification {
	@Accessor("VALUES_MAP")
	public static Map<String, EntityClassification> getValuesMap() {
		throw new AssertionError();
	}
}
