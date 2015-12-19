import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.EOPD;
import lejos.robotics.navigation.ArcRotateMoveController;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.MoveController;
import lejos.util.Delay;
import lejos.util.Timer;
import lejos.util.TimerListener;

class Main {
    static boolean enteredTapeWall(LightSensor lightSensor) {
        return lightSensor.getLightValue() <= 40;
    }

    static boolean tooCloseToFrontWall(UltrasonicSensor ultrasonicSensor) {
        return ultrasonicSensor.getDistance() <= 16;
    }

    static boolean tooCloseToSideWall(EOPD eopdSensor) {
        return eopdSensor.processedValue() >= 15;
    }

    public static void main(String[] args) {
        ArcRotateMoveController controller = new DifferentialPilot(
            MoveController.WHEEL_SIZE_RCX, 10.05, Motor.C, Motor.A);
        controller.setTravelSpeed(controller.getTravelSpeed() * 1);
        controller.setRotateSpeed(controller.getRotateSpeed() * 1);
        UltrasonicSensor ultrasonicSensor = new UltrasonicSensor(SensorPort.S1);
        EOPD eopdSensor = new EOPD(SensorPort.S3);
        LightSensor lightSensor = new LightSensor(SensorPort.S4);
        // Wait for the floodlight to turn on fully.
        Delay.msDelay(1000);

        long start = System.currentTimeMillis();
        Timer timer = new Timer(1000, new TimerListener() {
            @Override
            public void timedOut() {
                LCD.drawInt((int)((System.currentTimeMillis() - start) / 1000), 0, 0);
            }
        });
        timer.start();
        loop:
        while (true) {
            controller.arcForward(25);

            while (!(enteredTapeWall(lightSensor)
                || tooCloseToFrontWall(ultrasonicSensor)
                || tooCloseToSideWall(eopdSensor))) {
                if (Button.ENTER.isDown()) {
                    break loop;
                }
            }

            controller.travel(-5, true);
            while (controller.isMoving()) {
                if (Button.ENTER.isDown()) {
                    break loop;
                }
                Thread.yield();
            }

            controller.rotate(-45, true);
            while (controller.isMoving()) {
                if (Button.ENTER.isDown()) {
                    break loop;
                }
                Thread.yield();
            }
        }
        timer.stop();
        Button.ENTER.waitForPressAndRelease();
        Button.waitForAnyPress();
    }
}
