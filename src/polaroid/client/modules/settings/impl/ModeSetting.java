package polaroid.client.modules.settings.impl;


import polaroid.client.modules.settings.Setting;

import java.util.function.Supplier;

public class ModeSetting extends Setting<String> {

    public String[] strings;

    public ModeSetting(String name, String defaultVal, String... strings) {
        super(name, defaultVal);
        this.strings = strings;
    }

    public int getIndex() {
        int index = 0;
        for (String val : strings) {
            if (val.equalsIgnoreCase(get())) {
                return index;
            }
            index++;
        }
        return 0;
    }
    
    public void setIndex(int index) {
        if (index >= 0 && index < strings.length) {
            set(strings[index]);
        }
    }

    public boolean is(String s) {
        return get().equalsIgnoreCase(s);
    }
    @Override
    public ModeSetting setVisible(Supplier<Boolean> bool) {
        return (ModeSetting) super.setVisible(bool);
    }

}

