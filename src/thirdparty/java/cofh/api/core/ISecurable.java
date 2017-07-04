package cofh.api.core;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;

public interface ISecurable {
   boolean canPlayerAccess(EntityPlayer var1);

   boolean setAccess(ISecurable.AccessMode var1);

   boolean setOwnerName(String var1);

   boolean setOwner(GameProfile var1);

   ISecurable.AccessMode getAccess();

   String getOwnerName();

   GameProfile getOwner();

   public static enum AccessMode {
      PUBLIC,
      FRIENDS,
      TEAM,
      PRIVATE;

      public boolean isPublic() {
         return this == PUBLIC;
      }

      public boolean isPrivate() {
         return this == PRIVATE;
      }

      public boolean isTeamOnly() {
         return this == TEAM;
      }

      public boolean isFriendsOnly() {
         return this == FRIENDS;
      }

      public static ISecurable.AccessMode stepForward(ISecurable.AccessMode curAccess) {
         return curAccess == PUBLIC?TEAM:(curAccess == TEAM?FRIENDS:(curAccess == FRIENDS?PRIVATE:PUBLIC));
      }

      public static ISecurable.AccessMode stepBackward(ISecurable.AccessMode curAccess) {
         return curAccess == PUBLIC?PRIVATE:(curAccess == PRIVATE?FRIENDS:(curAccess == FRIENDS?TEAM:PUBLIC));
      }
   }
}
