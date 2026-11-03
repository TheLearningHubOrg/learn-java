// Stage 2: methods, collections and streams.
//   java examples/WordFrequency.java

record WordCount(String word, long count) {}

static final String TEXT = """
        the quick brown fox jumps over the lazy dog
        the dog sleeps and the fox runs
        """;

/** Splits on anything that is not a letter, lower-cases with a fixed locale, drops blanks. */
static List<String> words(String text) {
    return Arrays.stream(text.split("[^\\p{L}]+"))
            .filter(w -> !w.isBlank())
            .map(w -> w.toLowerCase(Locale.ROOT))
            .toList();
}

/** Imperative version: a HashMap and Map.merge. */
static Map<String, Long> countLoop(List<String> words) {
    Map<String, Long> counts = new HashMap<>();
    for (String w : words) {
        counts.merge(w, 1L, Long::sum);
    }
    return counts;
}

/** Declarative version: groupingBy + counting, collected into a TreeMap (sorted keys). */
static Map<String, Long> countStream(List<String> words) {
    return words.stream()
            .collect(Collectors.groupingBy(w -> w, TreeMap::new, Collectors.counting()));
}

static List<WordCount> top(Map<String, Long> counts, int n) {
    return counts.entrySet().stream()
            .map(e -> new WordCount(e.getKey(), e.getValue()))
            .sorted(Comparator.comparingLong(WordCount::count).reversed()
                    .thenComparing(WordCount::word))
            .limit(n)
            .toList();
}

void main() {
    List<String> words = words(TEXT);
    Map<String, Long> loop = countLoop(words);
    Map<String, Long> stream = countStream(words);

    if (!loop.equals(stream)) {
        throw new IllegalStateException("the two counting styles disagree");
    }

    IO.println("words: " + words.size() + ", distinct: " + stream.size());
    for (WordCount wc : top(stream, 3)) {
        IO.println(wc.word() + " = " + wc.count());
    }

    var expected = List.of(new WordCount("the", 4), new WordCount("dog", 2), new WordCount("fox", 2));
    if (!top(stream, 3).equals(expected)) {
        throw new IllegalStateException("unexpected top 3: " + top(stream, 3));
    }
}
