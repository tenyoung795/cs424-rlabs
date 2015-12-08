import java.awt.Point;

public final class Node {
    public Direction parent;
    private int g;
    private final int h;
    private boolean enqueued;
    private boolean visited;
    private boolean obstacle = false;

    public Node(Point destination, Point point) {
        this.h = Math.abs(point.x - destination.x) + Math.abs(point.y - destination.y);
        reset();
    }

    public void reset() {
        g = Integer.MAX_VALUE;
        enqueued = false;
        visited = false;
    }

    public int f() {
        return g == Integer.MAX_VALUE ? g : (g + h);
    }

    public boolean isEnqueued() {
        return enqueued;
    }

    public void enqueueAsSource() {
        g = 0;
        enqueued = true;
    }

    public boolean check(Direction currentDirection, Direction neighborDirection) {
        if (obstacle || visited) {
            return false;
        }
        int tentativeG = currentDirection.cost(neighborDirection);
        if (tentativeG < g) {
            parent = neighborDirection.flip();
            g = tentativeG;
        }
        if (enqueued) {
            return false;
        }
        enqueued = true;
        return true;
    }

    public boolean isVisited() {
        return visited;
    }

    public void visit() {
        enqueued = false;
        visited = true;
    }

    public boolean isObstacle() {
        return obstacle;
    }

    public void markObstacle() {
        obstacle = true;
    }
}
