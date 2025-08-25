package com.walhay.gregifiedenergistics.mixins.appeng;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import com.walhay.gregifiedenergistics.api.patterns.implementations.RecipePatternHelper;
import com.walhay.gregifiedenergistics.api.patterns.substitutions.AESubstitutionStorage;
import com.walhay.gregifiedenergistics.api.patterns.substitutions.SubstitutionStorage;
import com.walhay.gregifiedenergistics.mixins.interfaces.IResearchRecipeMapAccessor;
import com.walhay.gregifiedenergistics.mixins.interfaces.ITaskProgressAccessor;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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

	@Inject(
			method = "writeToNBT",
			at =
					@At(
							value = "INVOKE_ASSIGN",
							target =
									"Lappeng/me/cluster/implementations/CraftingCPUCluster;writeItem(Lappeng/api/storage/data/IAEItemStack;)Lnet/minecraft/nbt/NBTTagCompound;",
							ordinal = 1),
			locals = LocalCapture.CAPTURE_FAILHARD,
			remap = false)
	private void writeRecipeTask(
			NBTTagCompound data,
			CallbackInfo ci,
			NBTTagList list,
			Iterator<Object> iter,
			Map.Entry<ICraftingPatternDetails, Object> entry,
			NBTTagCompound item) {
		if (entry.getKey() instanceof RecipePatternHelper helper) {
			Recipe recipe = helper.getRecipe();

			IResearchRecipeMapAccessor researchMap = (IResearchRecipeMapAccessor) RecipeMaps.ASSEMBLY_LINE_RECIPES;

			if (researchMap == null) return;
			int recipeId = researchMap.getIdFromRecipe(recipe);

			if (recipeId < 0) return;

			item.setInteger("recipeId", recipeId);

			ISubstitutionStorage storage = helper.getSubstitutionStorage();
			if (storage != null) item.setTag(SubstitutionStorage.STORAGE_TAG, storage.serializeNBT());
		}
	}

	@Inject(
			method = "readFromNBT",
			at =
					@At(
							value = "INVOKE_ASSIGN",
							target =
									"Lappeng/util/item/AEItemStack;fromNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lappeng/api/storage/data/IAEItemStack;",
							ordinal = 1),
			locals = LocalCapture.CAPTURE_FAILHARD,
			remap = false)
	private void readRecipeTask(
			NBTTagCompound data, CallbackInfo ci, NBTTagList list, int x, NBTTagCompound item, IAEItemStack pattern)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ItemStack stack = ItemStack.EMPTY;
		if (pattern != null) stack = pattern.createItemStack();

		var taskProgress = taskProgressConstructor.newInstance();
		if (taskProgress instanceof ITaskProgressAccessor accessor) accessor.setValue(item.getLong("craftingProgress"));

		if (item.hasKey("recipeId")) {
			int recipeId = item.getInteger("recipeId");

			if (recipeId < 0) return;

			IResearchRecipeMapAccessor researchMap = (IResearchRecipeMapAccessor) RecipeMaps.ASSEMBLY_LINE_RECIPES;
			if (researchMap == null) return;

			Recipe recipe = researchMap.getRecipeById(recipeId);
			if (recipe == null) return;

			RecipePatternHelper helper = new RecipePatternHelper(recipe, stack);

			if (item.hasKey(SubstitutionStorage.STORAGE_TAG)) {
				ISubstitutionStorage storage =
						new AESubstitutionStorage(item.getCompoundTag(SubstitutionStorage.STORAGE_TAG));

				helper.injectSubstitutions(storage);
			}

			tasks.put(helper, taskProgress);
		}
	}
}
