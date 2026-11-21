import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

class FileResult {
    final String file;
    final boolean success;
    final double score;
    final String message;

    FileResult(String f, boolean s, double score, String m) {
        this.file = f;
        this.success = s;
        this.score = score;
        this.message = m;
    }
}

class ScoreFileTask implements Callable<FileResult> {
    private final Path path;

    ScoreFileTask(Path path) {
        this.path = path;
    }

    @Override
    public FileResult call() {
        try {
            String text = Files.readString(path).trim();
            if (text.isEmpty()) {
                throw new IllegalArgumentException("Empty score file");
            }
            
            String[] fields = text.split("\\s+");
            double sum = 0;
            for (String field : fields) {
                double mark = Double.parseDouble(field);
                if (mark < 0 || mark > 100 || Double.isNaN(mark)) {
                    throw new IllegalArgumentException("Score out of range: " + mark);
                }
                sum += mark;
            }
            
            double average = sum / fields.length;
            return new FileResult(path.toString(), true, average, "OK");
            
        } catch (NoSuchFileException e) {
            return new FileResult(path.toString(), false, 0, "Missing file");
        } catch (IOException | IllegalArgumentException e) {
            return new FileResult(path.toString(), false, 0, e.getMessage());
        }
    }
}

public class ResultProcessor {
    public static void main(String[] args) throws InterruptedException {
        int p = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        Path manifestPath = Paths.get("manifest.txt");

        // Automatically generate test data if manifest.txt is missing
        if (!Files.exists(manifestPath)) {
            System.out.println("manifest.txt not found. Generating 10,000 test files...");
            generateTestDataset(manifestPath, 10000);
        }

        List<Path> expectedFiles = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(manifestPath);
            for (String line : lines) {
                if (!line.isBlank()) {
                    expectedFiles.add(Paths.get(line.trim()));
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot read manifest file: " + e.getMessage());
            return;
        }

        ExecutorService pool = Executors.newFixedThreadPool(p);
        ConcurrentLinkedQueue<FileResult> results = new ConcurrentLinkedQueue<>();
        List<Future<FileResult>> futures = new ArrayList<>();

        long start = System.nanoTime();

        for (Path path : expectedFiles) {
            futures.add(pool.submit(new ScoreFileTask(path)));
        }

        for (Future<FileResult> future : futures) {
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                results.add(new FileResult("unknown", false, 0, "Task failure: " + e.getCause()));
            }
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);

        long elapsed = (System.nanoTime() - start) / 1_000_000;
        long success = results.stream().filter(r -> r.success).count();
        long failure = results.size() - success;

        System.out.println("Expected files (from manifest): " + expectedFiles.size());
        System.out.println("Successes: " + success);
        System.out.println("Failures: " + failure);
        System.out.println("Summary complete: " + (success + failure == expectedFiles.size()));
        System.out.println("Elapsed ms (pool " + p + "): " + elapsed);
    }

    private static void generateTestDataset(Path manifestPath, int totalFiles) {
        try {
            Path folder = Paths.get("scores");
            Files.createDirectories(folder);
            List<String> manifestEntries = new ArrayList<>();

            for (int i = 1; i <= totalFiles; i++) {
                Path filePath = folder.resolve("student_" + i + ".txt");
                manifestEntries.add(filePath.toString());

                if (i % 100 == 0) {
                    // Simulate 100 missing files
                    continue;
                } else if (i % 50 == 0) {
                    // Simulate 100 corrupt files
                    Files.writeString(filePath, "85 90 105 INVALID");
                } else {
                    // 9,800 valid score files
                    Files.writeString(filePath, "78 84 91 88");
                }
            }
            Files.write(manifestPath, manifestEntries);
            System.out.println("Generated manifest.txt and scores/ folder.");
        } catch (IOException e) {
            System.err.println("Setup failed: " + e.getMessage());
        }
    }
}
