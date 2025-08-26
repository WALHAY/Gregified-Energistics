package com.walhay.gregifiedenergistics.api.patterns.substitutions;

import com.walhay.gregifiedenergistics.api.capability.GregifiedEnergisticsCapabilities;
import com.walhay.gregifiedenergistics.api.capability.GregifiedEnergisticsDataCodes;
import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionNotifiable;
import com.walhay.gregifiedenergistics.api.patterns.ISubstitutionStorage;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import org.jetbrains.annotations.NotNull;

public class SubstitutionStorage extends MTETrait implements ISubstitutionStorage {

	public static final String STORAGE_TAG = "SubstitutionStorage";

	private final Object2IntOpenHashMap<String> substitutionMap = new Object2IntOpenHashMap<>();
	private ISubstitutionNotifiable notifiable;

	public SubstitutionStorage(MetaTileEntity mte) {
		super(mte);
		if (mte instanceof ISubstitutionNotifiable) this.notifiable = (ISubstitutionNotifiable) mte;
	}

	protected void onSubstitutionChange() {
		if (notifiable != null) notifiable.notifySubstitutionChange();
	}

	@Override
	public int getOption(String name) {
		if (!substitutionMap.containsKey(name)) {
			substitutionMap.put(name, 0);
			writeCustomData(GregifiedEnergisticsDataCodes.SUBSTITUTION_CHANGE, buf -> buf.writeString(name)
					.writeInt(0));
			return 0;
		}

		return substitutionMap.getInt(name);
	}

	@Override
	public void setOption(String name, int option) {
		if (substitutionMap.getInt(name) != option) {
			substitutionMap.put(name, option);
			writeCustomData(GregifiedEnergisticsDataCodes.SUBSTITUTION_CHANGE, buf -> buf.writeString(name)
					.writeInt(option));
			onSubstitutionChange();
		}
	}

	@Override
	public Collection<String> getOptions() {
		return substitutionMap.keySet();
	}

	@Override
	public <T> T getCapability(Capability<T> capability) {
		if (capability == GregifiedEnergisticsCapabilities.CAPABILITY_SUBSTITUTION_STORAGE) {
			return GregifiedEnergisticsCapabilities.CAPABILITY_SUBSTITUTION_STORAGE.cast(this);
		}
		return null;
	}

	@Override
	public String getName() {
		return STORAGE_TAG;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (Map.Entry<String, Integer> entry : substitutionMap.entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();

			tag.setString("Ingredient", entry.getKey());
			tag.setInteger("Option", entry.getValue());

			list.appendTag(tag);
		}

		compound.setTag("Data", list);

		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound compound) {
		NBTTagList nbt = compound.getTagList("Data", NBT.TAG_COMPOUND);
		for (NBTBase base : nbt) {
			if (base instanceof NBTTagCompound tag) {
				String key = tag.getString("Ingredient");
				int option = tag.getInteger("Option");

				substitutionMap.put(key, option);
			}
		}
	}

	@Override
	public void writeInitialSyncData(@NotNull PacketBuffer buf) {
		super.writeInitialSyncData(buf);
		buf.writeCompoundTag(serializeNBT());
	}

	@Override
	public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
		super.receiveInitialSyncData(buf);
		try {
			NBTTagCompound nbt = buf.readCompoundTag();

			if (nbt == null) return;

			deserializeNBT(nbt);
		} catch (IOException ignored) {
		}
	}

	@Override
	public void receiveCustomData(int discriminator, @NotNull PacketBuffer buf) {
		super.receiveCustomData(discriminator, buf);
		if (discriminator == GregifiedEnergisticsDataCodes.SUBSTITUTION_CHANGE) {
			String name = buf.readString(64);
			int option = buf.readInt();

			substitutionMap.put(name, option);
		}
	}
}
