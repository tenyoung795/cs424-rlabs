import java.awt.Point;

public enum Direction {
    RIGHT {
        public Orientation orientation() {
            return Orientation.HORIZONTAL;
        }

        public Direction flip() {
            return Direction.LEFT;
        }

        public void move(Point point) {
            point.translate(1, 0);
        }
    }, UP {
        public Orientation orientation() {
            return Orientation.VERTICAL;
        }

        public Direction flip() {
            return Direction.DOWN;
        }

        public void move(Point point) {
            point.translate(0, 1);
        }
    }, LEFT {
        public Orientation orientation() {
            return Orientation.HORIZONTAL;
        }

        public Direction flip() {
            return Direction.RIGHT;
        }

        public void move(Point point) {
            point.translate(-1, 0);
        }
    }, DOWN {
        public Orientation orientation() {
            return Orientation.VERTICAL;
        }

        public Direction flip() {
            return Direction.UP;
        }

        public void move(Point point) {
            point.translate(0, -1);
        }
    };

    public abstract Orientation orientation();
    public abstract Direction flip();
    public abstract void move(Point point);

    public int cost(Direction to) {
        return this.orientation() == to.orientation()
            ? (this == to ? 1 : 3)
            : 2;
    }
}
