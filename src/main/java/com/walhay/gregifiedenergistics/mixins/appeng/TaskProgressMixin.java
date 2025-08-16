package com.walhay.gregifiedenergistics.mixins.appeng;

import com.walhay.gregifiedenergistics.mixins.interfaces.ITaskProgressAccessor;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "appeng.me.cluster.implementations.CraftingCPUCluster$TaskProgress")
@Implements(@Interface(iface = ITaskProgressAccessor.class, prefix = "access$"))
public class TaskProgressMixin implements ITaskProgressAccessor {

	@Shadow(remap = false)
	public long value;

	@Override
	public void setValue(long value) {
		this.value = value;
	}
}
