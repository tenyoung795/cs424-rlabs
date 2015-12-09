import java.awt.Point;
import javax.microedition.lcdui.Graphics;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.nxt.Button;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.EOPD;

class Main {
    public static Track whichTrack(int num) {
        switch (num) {
            case 1:
                return new Track(
                    8, 9, new Point(0, 0), new Point(4, 8),
                    new Point(0, 2), new Point(1, 2), new Point(2, 2), new Point(6, 2), new Point(7, 2),
                    new Point(3, 6), new Point(4, 6), new Point(5, 6), new Point(6, 6), new Point(7, 6));
            case 2:
                return new Track(
                    7, 8, new Point(3, 0), new Point(2, 5),
                    new Point(2, 0), new Point(4, 0),
                    new Point(2, 2), new Point(4, 2),
                    new Point(0, 3), new Point(1, 3), new Point(2, 3), new Point(4, 3),
                    new Point(4, 4), new Point(4, 5));
            case 3:
                return new Track(5, 8, new Point(0, 0), new Point(0, 7));
            default:
                throw new IllegalArgumentException(num + " must be 1, 2, or 3");
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Track track = whichTrack(3);
        Graphics graphics = new Graphics();
        DifferentialPilot pilot = new DifferentialPilot(8.16, 9.55, Motor.C, Motor.A);
        pilot.setAcceleration((int)(pilot.getMaxTravelSpeed() * 1));

        LightSensor lightSensor = new LightSensor(SensorPort.S1);
        EOPD eopdSensor = new EOPD(SensorPort.S3);
        Direction direction = Direction.UP;

        graphics.clear();
        track.displayMatrix(graphics, direction);

        while (!track.isDone()) {
            track.reset();
            Direction[] reversePath = track.aStar(direction);

            track.displayMatrix(graphics, direction);

            direction = track.follow(pilot, lightSensor, eopdSensor,
                                     graphics, direction, reversePath);
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println(duration / 1000);
        Button.ESCAPE.waitForPressAndRelease();
    }
}
