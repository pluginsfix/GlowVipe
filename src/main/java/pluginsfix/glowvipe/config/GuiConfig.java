package pluginsfix.glowvipe.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.List;

public record GuiConfig(
        String title,
        String slotFreeName,
        List<String> slotFreeLore,
        String buySlotName,
        List<String> buySlotLore,
        String buttonPackName,
        List<String> buttonPackLore,
        String buttonUnpackName,
        List<String> buttonUnpackLore
) {
    public static GuiConfig fromSection(ConfigurationSection section) {
        if (section == null) {
            return new GuiConfig(
                    "",
                    "",
                    Collections.emptyList(),
                    "",
                    Collections.emptyList(),
                    "",
                    Collections.emptyList(),
                    "",
                    Collections.emptyList()
            );
        }

        return new GuiConfig(
                section.getString("title", ""),
                section.getString("slot-free-name", ""),
                section.getStringList("slot-free-lore"),
                section.getString("buy-slot-name", ""),
                section.getStringList("buy-slot-lore"),
                section.getString("button-pack-name", ""),
                section.getStringList("button-pack-lore"),
                section.getString("button-unpack-name", ""),
                section.getStringList("button-unpack-lore")
        );
    }
}
