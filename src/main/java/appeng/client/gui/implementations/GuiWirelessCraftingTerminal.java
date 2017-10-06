package appeng.client.gui.implementations;

import appeng.container.implementations.ContainerWirelessCraftingTerminal;
import appeng.helpers.WirelessTerminalGuiObject;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiWirelessCraftingTerminal extends GuiCraftingTerm {
	public GuiWirelessCraftingTerminal(InventoryPlayer inventoryPlayer, WirelessTerminalGuiObject te) {
		super(inventoryPlayer, te, new ContainerWirelessCraftingTerminal(inventoryPlayer, te));
	}
}
