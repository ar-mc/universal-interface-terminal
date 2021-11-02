package ar.ne.mc.uit;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemPartUIT extends Item implements IPartItem<PartUIT> {
    public ItemPartUIT() {
        this.setUnlocalizedName(UniversalInterfaceTerminal.MOD_ID + ".universal_interface_terminal");
        this.setRegistryName("universal_interface_terminal");
        this.setMaxStackSize(64);
    }

    @Nullable
    @Override
    public PartUIT createPartFromItemStack(ItemStack is) {
        return new PartUIT(is);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, side, player, hand, world);
    }
}
