import java.awt.Point;

public enum Direction {
    RIGHT {
        @Override
        public Orientation orientation() {
            return Orientation.HORIZONTAL;
        }

        @Override
        public Direction flip() {
            return Direction.LEFT;
        }

        @Override
        public void move(Point point) {
            point.translate(1, 0);
        }

        @Override
        public Turn howToTurn(Direction to) {
            switch (to) {
                case RIGHT:
                    return Turn.FORWARD;
                case UP:
                    return Turn.LEFT_TURN;
                case LEFT:
                    return Turn.BACKWARD;
                case DOWN:
                    return Turn.RIGHT_TURN;
            }
            throw new AssertionError("Impossible Direction " + to);
        }
    }, UP {
        @Override
        public Orientation orientation() {
            return Orientation.VERTICAL;
        }

        @Override
        public Direction flip() {
            return Direction.DOWN;
        }

        @Override
        public void move(Point point) {
            point.translate(0, 1);
        }
    }, LEFT {
        @Override
        public Orientation orientation() {
            return Orientation.HORIZONTAL;
        }

        @Override
        public Direction flip() {
            return Direction.RIGHT;
        }

        @Override
        public void move(Point point) {
            point.translate(-1, 0);
        }
    }, DOWN {
        @Override
        public Orientation orientation() {
            return Orientation.VERTICAL;
        }

        @Override
        public Direction flip() {
            return Direction.UP;
        }

        @Override
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

    public Turn howToTurn(Direction to) {
        to = Direction.values()[(4 + to.ordinal() - this.ordinal()) % 4];
        return Direction.RIGHT.howToTurn(to);
    }
}
