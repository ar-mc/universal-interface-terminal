package ar.ne.mc.uit;

import appeng.api.networking.IGridHost;
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
import java.util.HashSet;
import java.util.logging.Logger;

import static ar.ne.mc.uit.UniversalInterfaceTerminal.MOD_ID;
import static ar.ne.mc.uit.UniversalInterfaceTerminal.NAME;

@Mod(modid = MOD_ID, name = NAME, dependencies = "required-after:appliedenergistics2")
@Mod.EventBusSubscriber(modid = MOD_ID)
public class UniversalInterfaceTerminal {
    public static final String MOD_ID = "universal_interface_terminal";
    public static final String NAME = "Universal Interface Terminal";
    public static final ItemPartUIT ITEM_PART_UIT = new ItemPartUIT();
    public static final HashSet<Class<? extends IGridHost>> CLASS_LIST = new HashSet<>();
    private static final Logger LOGGER = Logger.getLogger(MOD_ID);
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
        for (String c : UITConfig.ClassesReference) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends IGridHost> clazz = (Class<? extends IGridHost>) getClass().getClassLoader().loadClass("c");
                CLASS_LIST.add(clazz);
                LOGGER.info("Registered class " + clazz.getName());
            } catch (ClassNotFoundException e) {
                LOGGER.warning("Class " + c + " not found!");
                e.printStackTrace();
            }
        }
        LOGGER.info("Universal Interface Terminal loaded");
    }
}
