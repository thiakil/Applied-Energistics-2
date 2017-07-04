package cofh.api.energy;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.util.EnumFacing;

public interface IEnergyTransport extends IEnergyProvider, IEnergyReceiver {
   int getEnergyStored(EnumFacing var1);

   IEnergyTransport.InterfaceType getTransportState(EnumFacing var1);

   boolean setTransportState(IEnergyTransport.InterfaceType var1, EnumFacing var2);

   public static enum InterfaceType {
      SEND,
      RECEIVE,
      BALANCE;

      public IEnergyTransport.InterfaceType getOpposite() {
         return this == BALANCE?BALANCE:(this == SEND?RECEIVE:SEND);
      }

      public IEnergyTransport.InterfaceType rotate() {
         return this.rotate(true);
      }

      public IEnergyTransport.InterfaceType rotate(boolean forward) {
         return forward?(this == BALANCE?RECEIVE:(this == RECEIVE?SEND:BALANCE)):(this == BALANCE?SEND:(this == SEND?RECEIVE:BALANCE));
      }
   }
}
