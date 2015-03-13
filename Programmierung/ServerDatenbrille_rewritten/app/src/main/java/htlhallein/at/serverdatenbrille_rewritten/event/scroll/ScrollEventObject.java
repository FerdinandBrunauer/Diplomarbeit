package htlhallein.at.serverdatenbrille_rewritten.event.scroll;

import java.util.EventObject;

public class ScrollEventObject extends EventObject {
    private ScrollEventDirection direction;
    private int percent;

    public ScrollEventObject(Object source, ScrollEventDirection direction, int percent) {
        super(source);
        this.direction = direction;
        this.percent = percent;
    }

    public ScrollEventDirection getDirection() {
        return this.direction;
    }

    public void setDirection(ScrollEventDirection direction) {
        this.direction = direction;
    }

    public int getPercent() {
        return this.percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }
}


