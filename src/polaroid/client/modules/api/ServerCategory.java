package polaroid.client.modules.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

@Getter
@AllArgsConstructor
public enum ServerCategory {

    RW("RW", new Color(0xFFA600)),
    FT("FT", new Color(0xFF243D)),
    HW("HW", new Color(0x24E5FF)),
    NO("", new Color(0x0024E5FF, true));

    private final String name;
    private final Color color;



    public static Color getColorByGroupName(String groupName) {
        for (ServerCategory group : values()) {
            if (group.getName().equals(groupName)) {
                return group.getColor();
            }
        }
        return Color.WHITE;
    }
}

