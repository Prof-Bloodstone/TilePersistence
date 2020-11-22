
/*
 *  tilepersistence - Persist TEs name and lore
 *  Copyright (C) 2020  Prof_Bloodstone
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package dev.bloodstone.tilepersistence

import dev.bloodstone.mcutils.datatypes.JsonDataType
import io.papermc.lib.PaperLib
import io.papermc.lib.features.blockstatesnapshot.BlockStateSnapshotResult
import java.util.Arrays
import org.bukkit.NamespacedKey
import org.bukkit.block.TileState
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

public class TilePersistence() : JavaPlugin(), Listener {
    private val NAME_KEY: NamespacedKey = NamespacedKey(this, "name")
    private val LORE_KEY: NamespacedKey = NamespacedKey(this, "lore")
    private val LORE_PDT: PersistentDataType<String, Array<String>> = JsonDataType(Array<String>::class.java)

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
    }

    @EventHandler
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        val block = event.blockPlaced
        val blockStateSnapshotResult: BlockStateSnapshotResult = PaperLib.getBlockState(block, false)
        val headItem = event.itemInHand
        val meta = headItem.itemMeta ?: return
        val name = meta.displayName
        val lore = meta.lore
        val blockState = blockStateSnapshotResult.getState() as? TileState ?: return
        val blockPDC = blockState.persistentDataContainer
        blockPDC.set(NAME_KEY, PersistentDataType.STRING, name)
        if (lore != null) blockPDC.set(LORE_KEY, LORE_PDT, lore.toTypedArray())
        if (blockStateSnapshotResult.isSnapshot()) blockState.update()
    }

    @EventHandler
    fun onBlockDropItemEvent(event: BlockDropItemEvent) {
        val blockState = event.blockState as? TileState ?: return
        val blockPDC = blockState.persistentDataContainer
        val name = blockPDC.get(NAME_KEY, PersistentDataType.STRING)
        val lore = blockPDC.get(LORE_KEY, LORE_PDT)
        if (name == null && lore == null) return
        for (item in event.items) { // Ideally should only be one...
            val itemstack = item.itemStack
            // This shouldn't happen
            val meta = itemstack.itemMeta ?: continue
            if (name != null) meta.setDisplayName(name)
            if (lore != null) meta.lore = Arrays.asList(*lore)
            itemstack.setItemMeta(meta)
        }
    }
}
