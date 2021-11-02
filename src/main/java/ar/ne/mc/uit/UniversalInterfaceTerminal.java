package ar.ne.mc.uit;

import appeng.core.sync.network.NetworkHandler;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

import static ar.ne.mc.uit.UniversalInterfaceTerminal.MOD_ID;
import static ar.ne.mc.uit.UniversalInterfaceTerminal.NAME;

@Mod(modid = MOD_ID, name = NAME, dependencies = "required-after:appliedenergistics2")
@Mod.EventBusSubscriber(modid = MOD_ID)
public class UniversalInterfaceTerminal {
    public static final String MOD_ID = "universal_interface_terminal";
    public static final String NAME = "Universal Interface Terminal";
    public static final ItemPartUIT ITEM_PART_UIT = new ItemPartUIT();
    @Nonnull
    private static final UniversalInterfaceTerminal INSTANCE = new UniversalInterfaceTerminal();

    @Nonnull
    @Mod.InstanceFactory
    public static UniversalInterfaceTerminal getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public static void onRegistry(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ITEM_PART_UIT);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(ITEM_PART_UIT, 0, new ModelResourceLocation(ITEM_PART_UIT.getRegistryName().toString()));
    }

    @Mod.EventHandler
    private void postInit(final FMLPostInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, EGuiBridge.GUI_Handler);
        NetworkHandler.init("AE2UNIVERSAL_INTERFACE_TERMINAL");
    }
}
