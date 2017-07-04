package cofh.api.tileentity;

public interface IReconfigurableFacing {
   int getFacing();

   boolean allowYAxisFacing();

   boolean rotateBlock();

   boolean setFacing(int var1);
}
