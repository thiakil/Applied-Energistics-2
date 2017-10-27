package appeng.util;

import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class TileEntityMappingFixer {
	private static Map<String, String> REPLACEMENTS = new HashMap<>();

	@SuppressWarnings("unused")//ASM Baby!
	public static void replaceOldIDs(NBTTagCompound tag){
		String id = tag.getString("id");
		if (REPLACEMENTS.containsKey(id)){
			tag.setString("id", REPLACEMENTS.get(id));
		}
	}

	public static void addLegacyTileEntityRemap(String from, String to){
		REPLACEMENTS.put(from, to);
	}
}
