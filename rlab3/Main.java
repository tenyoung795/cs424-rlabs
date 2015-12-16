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
import lejos.util.Timer;
import lejos.util.TimerListener;

class Main {
    static boolean enteredTapeWall(LightSensor lightSensor) {
        return lightSensor.getLightValue() <= 40;
    }

    public static void main(String[] args) {
        ArcRotateMoveController controller = new DifferentialPilot(
            MoveController.WHEEL_SIZE_RCX, 10.05, Motor.C, Motor.A);
        controller.setTravelSpeed(controller.getTravelSpeed() * 1);
        UltrasonicSensor ultrasonicSensor = new UltrasonicSensor(SensorPort.S1);
        EOPD eopdSensor = new EOPD(SensorPort.S3);
        LightSensor lightSensor = new LightSensor(SensorPort.S4);
        long start = System.currentTimeMillis();
        Timer timer = new Timer(1000, new TimerListener() {
            @Override
            public void timedOut() {
                LCD.drawInt((int)((System.currentTimeMillis() - start) / 1000), 0, 0);
            }
        });
        timer.start();
        while (true) {
            controller.arcForward(20);

            boolean done = false;
            while (!enteredTapeWall(lightSensor)) {
                if (Button.ENTER.isDown()) {
                    done = true;
                    break;
                }
                Thread.yield();
            }
            if (done) break;

            controller.rotate(-45);
        }
        timer.stop();
        Button.waitForAnyPress();
    }
}
