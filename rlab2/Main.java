import java.awt.Point;
import java.util.List;
import javax.microedition.lcdui.Graphics;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Navigator;
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
        Track track = whichTrack(1);
        Graphics graphics = new Graphics();
        Navigator navigator = new Navigator(new DifferentialPilot(85.5, 95.5, Motor.C, Motor.A));
        LightSensor lightSensor = new LightSensor(SensorPort.S1);
        EOPD eopdSensor = new EOPD(SensorPort.S3);
        Direction direction = Direction.UP;
        while (!track.isDone()) {
            graphics.clear();

            track.reset();
            List<Direction> reversePath = track.aStar(direction);

            track.displayMatrix(graphics);
            track.displayPath(graphics, direction, reversePath);

            direction = track.follow(navigator, lightSensor, eopdSensor, direction, reversePath);
        }
        Button.ESCAPE.waitForPressAndRelease();
    }
}
