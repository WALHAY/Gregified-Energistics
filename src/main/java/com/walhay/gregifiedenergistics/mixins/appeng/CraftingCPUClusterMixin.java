package com.walhay.gregifiedenergistics.mixins.appeng;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.item.AEItemStack;
import com.walhay.gregifiedenergistics.api.patterns.impl.DataStickPatternHelper;
import com.walhay.gregifiedenergistics.mixins.interfaces.ITaskProgressAccessor;
import gregtech.api.util.AssemblyLineManager;
import java.lang.reflect.Constructor;
import java.util.Map;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingCPUCluster.class)
public class CraftingCPUClusterMixin {

	@Shadow(remap = false)
	@Final
	private Map<ICraftingPatternDetails, Object> tasks;

	private static Constructor<?> taskProgressConstructor;

	static {
		Class<?> clazz = null;
		for (Class<?> innerClass : CraftingCPUCluster.class.getDeclaredClasses()) {
			if (innerClass.getSimpleName().equals("TaskProgress")) {
				clazz = innerClass;
				break;
			}
		}

		try {
			taskProgressConstructor = clazz.getDeclaredConstructor();
			taskProgressConstructor.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Inject(method = "readFromNBT", at = @At(value = "HEAD"), remap = false)
	private void injectCustomPatterns(NBTTagCompound data, CallbackInfo ci) {
		NBTTagList list = data.getTagList("tasks", 10);
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound nbt = list.getCompoundTagAt(i);

			IAEItemStack pattern = AEItemStack.fromNBT(nbt);
			if (pattern != null && pattern.getItem() != null) {
				ItemStack stack = pattern.createItemStack();

				if (AssemblyLineManager.isStackDataItem(stack, true)) {
					DataStickPatternHelper helper = new DataStickPatternHelper(stack);

					try {

						var tp = taskProgressConstructor.newInstance();
						if (tp instanceof ITaskProgressAccessor accessor)
							accessor.setValue(nbt.getLong("craftingProgress"));

						helper.getPatterns().forEach(p -> tasks.put(p, tp));
					} catch (Exception e) {

					}
				}
			}
		}
	}
}
