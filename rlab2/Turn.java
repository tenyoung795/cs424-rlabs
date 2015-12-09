import lejos.robotics.navigation.DifferentialPilot;

public enum Turn {
    FORWARD {
        @Override
        public void perform(DifferentialPilot pilot) {
        }
    }, RIGHT_TURN {
        @Override
        public void perform(DifferentialPilot pilot) {
            pilot.rotate(-110);
        }
    }, BACKWARD {
        @Override
        public void perform(DifferentialPilot pilot) {
            pilot.rotate(190);
        }
    }, LEFT_TURN {
        @Override
        public void perform(DifferentialPilot pilot) {
            pilot.rotate(110);
        }
    };

    public abstract void perform(DifferentialPilot pilot);
}
