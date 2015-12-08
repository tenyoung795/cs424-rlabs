import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.lcdui.Graphics;
import lejos.robotics.navigation.MoveController;
import lejos.robotics.navigation.Navigator;
import lejos.nxt.LightSensor;
import lejos.nxt.addon.EOPD;

public final class Track {

    private final int width, height;
    private final Point source, destination;
    private final Node[][] matrix;

    public Track(int width, int height,
                 Point source, Point destination,
                 Point... obstacles) {
        this.width = width;
        this.height = height;
        this.source = new Point(source);
        this.destination = new Point(destination);
        matrix = new Node[height][width];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                matrix[y][x] = new Node(destination, new Point(x, y));
            }
        }
        for (Point obstacle : obstacles) {
            matrix[obstacle.y][obstacle.x].markObstacle();
        }
    }

    public void reset() {
        for (Node[] row : matrix) {
            for (Node node : row) {
                node.reset();
            }
        }
    }

    public boolean isDone() {
        return source.equals(destination);
    }

    public Point locateMinF() {
        Point result = null;
        int minF = Integer.MAX_VALUE;
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                int f = matrix[x][y].f();
                if (f < minF) {
                    result = new Point(x, y);
                    minF = f;
                }
            }
        }
        return result;
    }

    public List<Direction> aStar(Direction direction) {
        List<Direction> reversePath = new ArrayList<>(width * height);
        matrix[source.y][source.x].enqueueAsSource();

        boolean foundDestination = false;
        int queueSize = 1;
        while (queueSize > 0 || !foundDestination) {
            Point p = locateMinF();
            matrix[p.y][p.x].visit();
            --queueSize;

            if (destination.equals(p)) {
                foundDestination = true;
            } else {
                if (p.x < width - 1 && matrix[p.y][p.x+1].check(direction, Direction.RIGHT)) {
                    ++queueSize;
                }
                if (p.y < height - 1 && matrix[p.y+1][p.x].check(direction, Direction.UP)) {
                    ++queueSize;
                }
                if (p.x > 0 && matrix[p.y][p.x-1].check(direction, Direction.LEFT)) {
                    ++queueSize;
                }
                if (p.y > 0 && matrix[p.y-1][p.x].check(direction, Direction.DOWN)) {
                    ++queueSize;
                }
            }
        }

        if (!foundDestination) {
            throw new IllegalStateException("Impossible track");
        }

        Point p = new Point(destination);
        while (!source.equals(p)) {
            Direction parent = matrix[p.y][p.x].parent;
            reversePath.add(parent);
            parent.move(p);
        }
        return reversePath;
    }

    public void displayMatrix(Graphics graphics) {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (matrix[y][x].isObstacle()) {
                    graphics.fillRect(x * 5, 63 - y * 5, 5, 5);
                } else {
                    graphics.drawRect(x * 5, 63 - y * 5, 5, 5);
                }
            }
        }
    }

    public void displayPath(Graphics graphics,
                            Direction startDirection, List<Direction> reversePath) {
        // Draws a < in the starting direction.
        switch (startDirection) {
            case RIGHT:
                graphics.drawLine(source.x * 5, 63 - (5 + source.y * 5),
                                  5 + source.x * 5, 63 - (2 + source.y * 5));
                graphics.drawLine(5 + source.x * 5, 63 - (2 + source.y * 5),
                                  source.x * 5, 63 - source.y * 5);
                break;
            case UP:
                graphics.drawLine(source.x * 5, 63 - source.y * 5,
                                  2 + source.x * 5, 63 - (5 + source.y * 5));
                graphics.drawLine(2 + source.x * 5, 63 - (5 + source.y * 5),
                                  5 + source.x * 5, 63 - source.y * 5);
                break;
            case LEFT:
                graphics.drawLine(5 + source.x * 5, 63 - (5 + source.y * 5),
                                  source.x * 5, 63 - (2 + source.y * 5));
                graphics.drawLine(source.x * 5, 63 - (2 + source.y * 5),
                                  5 + source.x * 5, 63 - source.y * 5);
                break;
            case DOWN:
                graphics.drawLine(source.x * 5, 63 - (5 + source.y * 5),
                                  2 + source.x * 5, 63 - source.y * 5);
                graphics.drawLine(2 + source.x * 5, 63 - source.y * 5,
                                  5 + source.x * 5, 63 - (5 + source.y * 5));
                break;
        }

        // Draws an X at the destination.
        graphics.drawLine(destination.x * 5, destination.y * 5,
                          5 + destination.x * 5, 5 + destination.y * 5);
        graphics.drawLine(destination.x * 5, 5 + destination.y * 5,
                          5 + destination.x * 5, destination.y * 5);

        Point p = new Point(destination);
        for (Direction direction : reversePath) {
            direction.move(p);
            graphics.fillRect(2 + p.x * 5, 63 - (2 + p.y * 5), 1, 1);
        }
    }

    Direction follow(Navigator navigator, LightSensor lightSensor, EOPD eopdSensor,
                     Direction direction, List<Direction> reversePath) {
        Point p = new Point(source);
        for (Direction reverseDirection : reversePath) {
            direction = reverseDirection.flip();
            direction.move(p);
            
            navigator.goTo(p.x - source.x, p.y - source.y);
            boolean foundObstacle = false;
            while (navigator.isMoving() && !foundObstacle) {
                if (lightSensor.getLightValue() <= 40 || eopdSensor.processedValue() >= 200) {
                    navigator.stop();
                    MoveController controller = navigator.getMoveController();
                    controller.travel(-controller.getMovement().getDistanceTraveled());
                    foundObstacle = true;
                }
            }
            if (foundObstacle) break;
        }
        source.setLocation(p);
        return direction;
    }
}
