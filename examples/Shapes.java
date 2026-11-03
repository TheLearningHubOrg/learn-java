// Stage 3: sealed types, records, pattern matching, generics and exceptions.
//   java examples/Shapes.java

sealed interface Shape permits Circle, Rect, Triangle {}

record Circle(double radius) implements Shape {
    Circle {
        if (radius <= 0) throw new IllegalArgumentException("radius must be positive: " + radius);
    }
}

record Rect(double width, double height) implements Shape {}

record Triangle(double a, double b, double c) implements Shape {
    Triangle {
        if (a + b <= c || a + c <= b || b + c <= a) {
            throw new IllegalArgumentException("not a triangle: " + a + ", " + b + ", " + c);
        }
    }
}

/** Exhaustive over the sealed hierarchy: no default branch, and a new Shape breaks compilation here. */
static double area(Shape shape) {
    return switch (shape) {
        case Circle(double r) -> Math.PI * r * r;
        case Rect(double w, double h) when w == h -> w * w;   // guard: a square
        case Rect(double w, double h) -> w * h;
        case Triangle(double a, double b, double c) -> {
            double s = (a + b + c) / 2;                          // Heron's formula
            yield Math.sqrt(s * (s - a) * (s - b) * (s - c));
        }
    };
}

/** A bounded generic method: works for any list of Shape subtypes. */
static <T extends Shape> T largest(List<? extends T> shapes) {
    if (shapes.isEmpty()) throw new NoSuchElementException("no shapes");
    T best = shapes.getFirst();
    for (T s : shapes) {
        if (area(s) > area(best)) best = s;
    }
    return best;
}

/** A checked exception for a recoverable, expected failure. */
static final class ShapeFormatException extends Exception {
    ShapeFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}

/** Parses "circle 2", "rect 3 4" or "triangle 3 4 5". */
static Shape parse(String line) throws ShapeFormatException {
    String[] p = line.strip().split("\\s+");
    try {
        return switch (p[0]) {
            case "circle" -> new Circle(Double.parseDouble(p[1]));
            case "rect" -> new Rect(Double.parseDouble(p[1]), Double.parseDouble(p[2]));
            case "triangle" -> new Triangle(Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]));
            default -> throw new ShapeFormatException("unknown shape '" + p[0] + "'", null);
        };
    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
        // NumberFormatException is a subclass of IllegalArgumentException.
        throw new ShapeFormatException("cannot parse '" + line + "': " + e.getMessage(), e);
    }
}

void main() {
    List<Shape> shapes = new ArrayList<>();
    List<String> errors = new ArrayList<>();
    for (String line : List.of("circle 1", "rect 2 2", "rect 2 5", "triangle 3 4 5", "hexagon 1", "rect two 3", "triangle 1 1 9")) {
        try {
            shapes.add(parse(line));
        } catch (ShapeFormatException e) {
            errors.add(e.getMessage());
        }
    }

    for (Shape s : shapes) {
        IO.println("%-40s area=%.2f".formatted(s, area(s)));
    }
    IO.println("largest: " + largest(shapes));
    errors.forEach(e -> IO.println("error: " + e));

    if (shapes.size() != 4 || errors.size() != 3 || !(largest(shapes) instanceof Rect(double w, double h) && w * h == 10.0)) {
        throw new IllegalStateException("unexpected result: " + shapes + " " + errors);
    }
}
