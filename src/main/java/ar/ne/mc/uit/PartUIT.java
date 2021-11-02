package ar.ne.mc.uit;

import appeng.api.parts.IPartModel;
import appeng.api.util.AEPartLocation;
import appeng.core.AppEng;
import appeng.core.sync.GuiHostType;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PartUIT extends PartInterfaceTerminal {
    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(AppEng.MOD_ID, "part/interface_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation(AppEng.MOD_ID, "part/interface_terminal_on");
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    public PartUIT(ItemStack is) {
        super(is);
    }

    public static void EOpenGUI(@Nonnull final EntityPlayer p, @Nullable final TileEntity tile, @Nullable final AEPartLocation side, @Nonnull final EGuiBridge type) {
        if (Platform.isClient()) {
            return;
        }

        int x = (int) p.posX;
        int y = (int) p.posY;
        int z = (int) p.posZ;
        if (tile != null) {
            x = tile.getPos().getX();
            y = tile.getPos().getY();
            z = tile.getPos().getZ();
        }

        if ((type.getType().isItem() && tile == null) || type.hasPermissions(tile, x, y, z, side, p)) {
            if (tile == null && type.getType() == GuiHostType.ITEM) {
                p.openGui(UniversalInterfaceTerminal.getInstance(), type.ordinal() << 4, p.getEntityWorld(), p.inventory.currentItem, 0, 0);
            } else if (tile == null || type.getType() == GuiHostType.ITEM) {
                p.openGui(UniversalInterfaceTerminal.getInstance(), type.ordinal() << 4 | (1 << 3), p.getEntityWorld(), x, y, z);
            } else {
                p.openGui(UniversalInterfaceTerminal.getInstance(), type.ordinal() << 4 | (side.ordinal()), tile.getWorld(), x, y, z);
            }
        }
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (Platform.isServer()) {
            EOpenGUI(player, this.getHost().getTile(), this.getSide(), EGuiBridge.GUI_INTERFACE_TERMINAL);
        }
        return true;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}
