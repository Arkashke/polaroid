package polaroid.client.modules.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {

    Combat("Combat","a"),
    Movement("Movement","b"),
    Render("Visuals","d"),
    Player("Player","c"),
    Misc("Miscellaneous", "e");
    private final String name;
    private final String icon;


}


