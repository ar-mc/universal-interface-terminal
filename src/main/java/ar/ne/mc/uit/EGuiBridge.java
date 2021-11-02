/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package ar.ne.mc.uit;


import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.exceptions.AppEngException;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.GuiNull;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.ContainerOpenContext;
import appeng.core.sync.GuiHostType;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Constructor;

import static appeng.core.sync.GuiHostType.ITEM;


public enum EGuiBridge implements IGuiHandler {

    GUI_Handler(),

    GUI_INTERFACE_TERMINAL(ContainerUIT.class, PartUIT.class, GuiHostType.WORLD, SecurityPermissions.BUILD);


    private final Class<?> tileClass;
    private final Class<?> containerClass;
    private final GuiHostType type;
    private final SecurityPermissions requiredPermission;
    private Class<?> guiClass;

    EGuiBridge(final Class<?> containerClass, final Class<?> tileClass, final GuiHostType type, final SecurityPermissions requiredPermission) {
        this.requiredPermission = requiredPermission;
        this.containerClass = containerClass;
        this.type = type;
        this.tileClass = tileClass;
        this.getGui();
    }

    EGuiBridge() {
        tileClass = null;
        containerClass = null;
        type = null;
        requiredPermission = null;
    }

    /**
     * I honestly wish I could just use the GuiClass Names myself, but I can't access them without MC's Server
     * Exploding.
     */
    private void getGui() {
        if (Platform.isClient()) {
            AEBaseGui.class.getName();

            final String guiClass = "appeng.client.gui.implementations.GuiInterfaceTerminal";
            this.guiClass = ReflectionHelper.getClass(this.getClass().getClassLoader(), guiClass);
            if (this.guiClass == null) {
                throw new IllegalStateException("Cannot Load class: " + guiClass);
            }
        }
    }

    @Override
    public Object getServerGuiElement(final int ordinal, final EntityPlayer player, final World w, final int x, final int y, final int z) {
        final AEPartLocation side = AEPartLocation.fromOrdinal(ordinal & 0x07);
        final EGuiBridge ID = GUI_INTERFACE_TERMINAL;
        final boolean stem = ((ordinal >> 3) & 1) == 1;
        if (ID.type.isItem()) {
            ItemStack it = ItemStack.EMPTY;
            if (stem) {
                it = player.inventory.getCurrentItem();
            } else if (x >= 0 && x < player.inventory.mainInventory.size()) {
                it = player.inventory.getStackInSlot(x);
            }
            final Object myItem = this.getGuiObject(it, player, w, x, y, z);
            if (myItem != null && ID.CorrectTileOrPart(myItem)) {
                return this.updateGui(ID.ConstructContainer(player.inventory, myItem), w, x, y, z, side, myItem);
            }
        }
        if (ID.type != ITEM) {
            final TileEntity TE = w.getTileEntity(new BlockPos(x, y, z));
            if (TE instanceof IPartHost) {
                ((IPartHost) TE).getPart(side);
                final IPart part = ((IPartHost) TE).getPart(side);
                if (ID.CorrectTileOrPart(part)) {
                    return this.updateGui(ID.ConstructContainer(player.inventory, part), w, x, y, z, side, part);
                }
            } else {
                if (ID.CorrectTileOrPart(TE)) {
                    return this.updateGui(ID.ConstructContainer(player.inventory, TE), w, x, y, z, side, TE);
                }
            }
        }
        return new ContainerNull();
    }

    private Object getGuiObject(final ItemStack it, final EntityPlayer player, final World w, final int x, final int y, final int z) {
        if (!it.isEmpty()) {
            if (it.getItem() instanceof IGuiItem) {
                return ((IGuiItem) it.getItem()).getGuiObject(it, w, new BlockPos(x, y, z));
            }

            final IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
            if (wh != null) {
                return new WirelessTerminalGuiObject(wh, it, player, w, x, y, z);
            }
        }

        return null;
    }

    public boolean CorrectTileOrPart(final Object tE) {
        if (this.tileClass == null) {
            throw new IllegalArgumentException("This Gui Cannot use the standard Handler.");
        }

        return this.tileClass.isInstance(tE);
    }

    private Object updateGui(final Object newContainer, final World w, final int x, final int y, final int z, final AEPartLocation side, final Object myItem) {
        if (newContainer instanceof AEBaseContainer) {
            final AEBaseContainer bc = (AEBaseContainer) newContainer;
            bc.setOpenContext(new ContainerOpenContext(myItem));
            bc.getOpenContext().setWorld(w);
            bc.getOpenContext().setX(x);
            bc.getOpenContext().setY(y);
            bc.getOpenContext().setZ(z);
            bc.getOpenContext().setSide(side);
        }

        return newContainer;
    }

    @SuppressWarnings("rawtypes")
    public Object ConstructContainer(final InventoryPlayer inventory, final Object tE) {
        try {
            final Constructor[] c = this.containerClass.getConstructors();
            return getObject(inventory, tE, c);
        } catch (final Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    @SuppressWarnings("rawtypes")
    private Object getObject(InventoryPlayer inventory, Object tE, Constructor[] c) throws AppEngException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        if (c.length == 0) {
            throw new AppEngException("Invalid Gui Class");
        }

        final Constructor target = this.findConstructor(c, inventory, tE);

        if (target == null) {
            throw new IllegalStateException("Cannot find " + this.containerClass.getName() + "( " + this.typeName(inventory) + ", " + this
                    .typeName(tE) + " )");
        }

        return target.newInstance(inventory, tE);
    }

    @SuppressWarnings("rawtypes, unchecked")
    private Constructor findConstructor(final Constructor[] c, final InventoryPlayer inventory, final Object tE) {
        for (final Constructor con : c) {
            final Class[] types = con.getParameterTypes();
            if (types.length == 2) {
                if (types[0].isAssignableFrom(inventory.getClass()) && types[1].isAssignableFrom(tE.getClass())) {
                    return con;
                }
            }
        }
        return null;
    }

    private String typeName(final Object inventory) {
        if (inventory == null) {
            return "NULL";
        }

        return inventory.getClass().getName();
    }

    @Override
    public Object getClientGuiElement(final int ordinal, final EntityPlayer player, final World w, final int x, final int y, final int z) {
        final AEPartLocation side = AEPartLocation.fromOrdinal(ordinal & 0x07);
        final EGuiBridge ID = values()[ordinal >> 4];
        final boolean stem = ((ordinal >> 3) & 1) == 1;
        if (ID.type.isItem()) {
            ItemStack it = ItemStack.EMPTY;
            if (stem) {
                it = player.inventory.getCurrentItem();
            } else if (x >= 0 && x < player.inventory.mainInventory.size()) {
                it = player.inventory.getStackInSlot(x);
            }
            final Object myItem = this.getGuiObject(it, player, w, x, y, z);
            if (myItem != null && ID.CorrectTileOrPart(myItem)) {
                return ID.ConstructGui(player.inventory, myItem);
            }
        }
        if (ID.type != ITEM) {
            final TileEntity TE = w.getTileEntity(new BlockPos(x, y, z));
            if (TE instanceof IPartHost) {
                ((IPartHost) TE).getPart(side);
                final IPart part = ((IPartHost) TE).getPart(side);
                if (ID.CorrectTileOrPart(part)) {
                    return ID.ConstructGui(player.inventory, part);
                }
            } else {
                if (ID.CorrectTileOrPart(TE)) {
                    return ID.ConstructGui(player.inventory, TE);
                }
            }
        }
        return new GuiNull(new ContainerNull());
    }

    @SuppressWarnings("rawtypes")
    public Object ConstructGui(final InventoryPlayer inventory, final Object tE) {
        try {
            final Constructor[] c = this.guiClass.getConstructors();
            return getObject(inventory, tE, c);
        } catch (final Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    public boolean hasPermissions(final TileEntity te, final int x, final int y, final int z, final AEPartLocation side, final EntityPlayer player) {
        final World w = player.getEntityWorld();
        final BlockPos pos = new BlockPos(x, y, z);

        if (Platform.hasPermissions(te != null ? new DimensionalCoord(te) : new DimensionalCoord(player.world, pos), player)) {
            if (this.type.isItem()) {
                final ItemStack it = player.inventory.getCurrentItem();
                if (!it.isEmpty() && it.getItem() instanceof IGuiItem) {
                    final Object myItem = ((IGuiItem) it.getItem()).getGuiObject(it, w, pos);
                    if (this.CorrectTileOrPart(myItem)) {
                        return true;
                    }
                }
            }

            if (this.type != ITEM) {
                final TileEntity TE = w.getTileEntity(pos);
                if (TE instanceof IPartHost) {
                    ((IPartHost) TE).getPart(side);
                    final IPart part = ((IPartHost) TE).getPart(side);
                    if (this.CorrectTileOrPart(part)) {
                        return this.securityCheck(part, player);
                    }
                } else {
                    if (this.CorrectTileOrPart(TE)) {
                        return this.securityCheck(TE, player);
                    }
                }
            }
        }
        return false;
    }

    private boolean securityCheck(final Object te, final EntityPlayer player) {
        if (te instanceof IActionHost && this.requiredPermission != null) {

            final IGridNode gn = ((IActionHost) te).getActionableNode();
            final IGrid g = gn.getGrid();

            final ISecurityGrid sg = g.getCache(ISecurityGrid.class);
            return sg.hasPermission(player, this.requiredPermission);
        }
        return true;
    }

    public GuiHostType getType() {
        return this.type;
    }

}
