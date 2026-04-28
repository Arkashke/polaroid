package polaroid.client.ui.schedules.rw.impl;

import polaroid.client.ui.schedules.rw.Schedule;
import polaroid.client.ui.schedules.rw.TimeType;

public class ScroogeSchedule
        extends Schedule {
    @Override
    public String getName() {
        return "Скрудж";
    }

    @Override
    public TimeType[] getTimes() {
        return new TimeType[]{TimeType.FIFTEEN_HALF};
    }
}


