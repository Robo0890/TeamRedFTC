package org.firstinspires.ftc.teamcode.FTCUtilities;

import java.util.ArrayList;

public class LinearSlideGroup {

    private int position = 0;

    private boolean active = false;

    private ArrayList<LinearSlide> slideGroup = new ArrayList<>();

    public LinearSlideGroup(LinearSlide... slides) {
        for (LinearSlide s : slides) {
            slideGroup.add(s);
        }

    }

    public void setActive(boolean active) {
        this.active = active;
        for (LinearSlide s : slideGroup) {
            Thread thread = new Thread(s);
            thread.start();
        }
    }

    public void setPosition(int position) {
        this.position = position;
        if (active) {
            for (LinearSlide slide : slideGroup) {
                slide.setTargetPosition(position);
            }
        }
    }



    public int getPosition() {
        return position;
    }

    public int getCurrentPosition() {
        return slideGroup.get(0).getCurrentPosition();
    }
}
