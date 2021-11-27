package ar.ne.mc.uit;

import net.minecraftforge.common.config.Config;

import static ar.ne.mc.uit.UniversalInterfaceTerminal.MOD_ID;

@Config(modid = MOD_ID)
public final class UITConfig {
    @Config.Comment({
            "Full class reference of the target interface name array.",
            "Typically any 'block' style interface have two class ref: 'Part' variant and 'Tile' variant.",
            "Default to AE2's ME Interface and Volumetric Flask's interface and DualInterface in AE2 Fluid Crafting."
    })
    @Config.RequiresMcRestart
    public static String[] ClassesReference = {
            "appeng.parts.misc.PartInterface",
            "appeng.tile.misc.TileInterface",
            "me.exz.volumetricflask.common.parts.PartOInterface",
            "me.exz.volumetricflask.common.tile.TileOInterface",
            "xyz.phanta.ae2fc.tile.TileDualInterface",
            "xyz.phanta.ae2fc.parts.PartDualInterface",
    };
}
