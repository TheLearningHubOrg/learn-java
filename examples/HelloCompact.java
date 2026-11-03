// Stage 1: a compact source file (final in JDK 25). Run it with no build step:
//   java examples/HelloCompact.java Ada
// There is no class declaration and no imports: the file is wrapped in an
// implicit class, and every package exported by java.base is imported.

void main(String[] args) {
    String name = args.length > 0 ? args[0] : "world";
    int feature = Runtime.version().feature();

    IO.println("Hello, " + name + "!");
    IO.println("Running on Java " + feature + " (" + System.getProperty("java.vendor") + ")");

    var primes = List.of(2, 3, 5, 7, 11);
    int sum = 0;
    for (int p : primes) {
        sum += p;
    }
    IO.println("Sum of " + primes + " = " + sum);

    if (sum != 28) {
        throw new IllegalStateException("expected 28, got " + sum);
    }
}
