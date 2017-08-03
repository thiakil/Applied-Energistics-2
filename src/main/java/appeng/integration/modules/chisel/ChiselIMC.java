package appeng.integration.modules.chisel;


public enum ChiselIMC {

	/**
	 * Adds a variation to a group.
	 *
	 * Use this to add a variation to a group. String syntax:
	 * <p>
	 * groupname|blockname|meta
	 * <p>
	 * An example would be {@code "mygroup|minecraft:dirt|1"} and this will add the vanilla dirt block with metadata 1 to the "mygroup" group, creating that group if need be.
	 */
	ADD_VARIATION("variation:add"),

	/**
	 * Removes a variation from a group.
	 *
	 * Use this to remove a variation from a group. String syntax:
	 * <p>
	 * groupname|blockname|meta
	 * <p>
	 * An example would be {@code "mygroup|minecraft:dirt|1"} and this will add the vanilla dirt block with metadata 1 to the "mygroup" group, creating that group if need be.
	 */
	REMOVE_VARIATION("variation:remove"),

	/**
	 * Registers an oredict name to a group. This can be used to automatically add all blocks with this oredict name to a group. String syntax:
	 * <p>
	 * groupname|oredictname
	 * <p>
	 * An example would be {@code "mygroup|plankWood"} which will add all blocks registered in the oredict as "plankWood" to your group called "mygroup".
	 */
	REGISTER_GROUP_ORE("group:ore");

	/**
	 * The IMC message key for this message type.
	 */
	public final String key;

	ChiselIMC(String key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return key;
	}

	/**
	 * The modid of Chisel so you can easily send IMC to this mod.
	 */
	public static final String CHISEL_MODID = "chisel";

	public static final String getModid() {
		return CHISEL_MODID;
	}
}
