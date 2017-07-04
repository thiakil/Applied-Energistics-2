package cofh.api.tileentity;

import net.minecraft.util.EnumFacing;

public interface IInventoryConnection {
   IInventoryConnection.ConnectionType canConnectInventory(EnumFacing var1);

   public static enum ConnectionType {
      DEFAULT,
      FORCE,
      DENY;

      public final boolean canConnect = this.ordinal() != 2;
      public final boolean forceConnect = this.ordinal() == 1;
   }
}
