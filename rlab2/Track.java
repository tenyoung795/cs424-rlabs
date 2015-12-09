import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.microedition.lcdui.Graphics;
import lejos.robotics.navigation.DifferentialPilot;
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
                Node node = matrix[y][x];
                if (node.isObstacle() || !node.isEnqueued()) continue;
                int f = node.f();
                if (f < minF) {
                    result = new Point(x, y);
                    minF = f;
                }
            }
        }
        if (result == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        return result;
    }

    public Direction[] aStar(Direction direction) {
        List<Direction> reversePath = new ArrayList<>(width * height);
        matrix[source.y][source.x].enqueueAsSource();

        boolean foundDestination = false;
        int queueSize = 1;
        while (queueSize > 0) {
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
        return reversePath.toArray(new Direction[reversePath.size()]);
    }

    public void displayMatrix(Graphics graphics,
                              Direction startDirection) {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (matrix[y][x].isObstacle()) {
                    graphics.fillRect(x * 5, 63 - (5 + y * 5), 6, 6);
                } else {
                    graphics.drawRect(x * 5, 63 - (5 + y * 5), 5, 5);
                }
            }
        }

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
        graphics.drawLine(destination.x * 5, 63 - destination.y * 5,
                          5 + destination.x * 5, 63 - (5 + destination.y * 5));
        graphics.drawLine(destination.x * 5, 63 - (5 + destination.y * 5),
                          5 + destination.x * 5, 63 - (destination.y * 5));
    }

    Direction follow(DifferentialPilot pilot, LightSensor lightSensor, EOPD eopdSensor,
                     Graphics graphics,
                     Direction direction, Direction reversePath[]) {
        for (int i = reversePath.length - 1; i >= 0; --i) {
            Direction newDirection = reversePath[i].flip();

            Turn turn = direction.howToTurn(newDirection);
            turn.perform(pilot);
            pilot.travel(30.48, true);
            
            boolean foundObstacle = false;
            while (pilot.isMoving() && !foundObstacle) {
                if (lightSensor.readValue() <= 35 || eopdSensor.processedValue() >= 32) {
                    pilot.stop();
                    pilot.travel(-15.24);
                    Point p = new Point(source);
                    newDirection.move(p);
                    matrix[p.y][p.x].markObstacle();
                    foundObstacle = true;
                }
            }

            direction = newDirection;
            if (foundObstacle) break;
            direction.move(source);
            graphics.fillRect(2 + source.x * 5, 63 - (2 + source.y * 5), 1, 1);
        }
        return direction;
    }
}
