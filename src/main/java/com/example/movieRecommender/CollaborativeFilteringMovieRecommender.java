package com.example.movieRecommender;

import java.util.*;

public class CollaborativeFilteringMovieRecommender {

    // User -> (Movie -> Rating)
    private static final Map<String, Map<String, Double>> userRatings = new HashMap<>();

    public static void invokeRecommender() {
        initializeRatings();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name (e.g., Alice, Bob, Charlie, Dave, Eve): ");
        String targetUser = scanner.nextLine();
        scanner.close();
        if (!userRatings.containsKey(targetUser)) {
            System.out.println("User not found in the dataset.");
            return;
        }

        List<String> recommendations = getRecommendations(targetUser);

        System.out.println("\nMovie recommendations for " + targetUser + ":");
        if (recommendations.isEmpty()) {
            System.out.println("No new recommendations available.");
        } else {
            for (String movie : recommendations) {
                System.out.println("- " + movie);
            }
        }
    }

    private static void initializeRatings() {
        userRatings.put("Alice", Map.ofEntries(
                Map.entry("Inception", 5.0), Map.entry("Titanic", 3.0), Map.entry("Avatar", 4.5), Map.entry("Joker", 4.0), Map.entry("Interstellar", 4.5),
                Map.entry("Frozen", 2.5), Map.entry("Iron Man", 5.0), Map.entry("Up", 3.0), Map.entry("Coco", 3.5), Map.entry("Avengers", 4.5),
                Map.entry("Guardians of the Galaxy", 3.5), Map.entry("The Shawshank Redemption", 5.0), Map.entry("Forrest Gump", 4.0),
                Map.entry("The Matrix", 4.5), Map.entry("Finding Nemo", 2.0), Map.entry("Spider-Man", 4.0), Map.entry("Madagascar", 3.0),
                Map.entry("Inside Out", 3.5), Map.entry("The Dark Knight", 5.0), Map.entry("La La Land", 2.5)
        ));

        userRatings.put("Bob", Map.ofEntries(
                Map.entry("Inception", 4.0), Map.entry("Titanic", 2.0), Map.entry("Avatar", 4.0), Map.entry("Interstellar", 5.0), Map.entry("Coco", 4.5),
                Map.entry("Frozen", 3.0), Map.entry("Avengers", 5.0), Map.entry("Black Panther", 4.0), Map.entry("Joker", 4.5), Map.entry("Toy Story", 3.0),
                Map.entry("Guardians of the Galaxy", 4.0), Map.entry("The Shawshank Redemption", 4.5), Map.entry("Forrest Gump", 5.0),
                Map.entry("The Matrix", 3.5), Map.entry("Finding Nemo", 3.0), Map.entry("Spider-Man", 4.5), Map.entry("Madagascar", 2.5),
                Map.entry("Inside Out", 3.0), Map.entry("The Dark Knight", 5.0), Map.entry("La La Land", 3.5)
        ));

        userRatings.put("Charlie", Map.ofEntries(
                Map.entry("Titanic", 5.0), Map.entry("Joker", 5.0), Map.entry("Interstellar", 4.0), Map.entry("Coco", 3.5), Map.entry("Up", 4.0),
                Map.entry("Frozen", 3.5), Map.entry("Toy Story", 4.5), Map.entry("Lion King", 5.0), Map.entry("Shrek", 4.0), Map.entry("Minions", 3.0),
                Map.entry("Guardians of the Galaxy", 2.0), Map.entry("The Shawshank Redemption", 4.5), Map.entry("Forrest Gump", 5.0),
                Map.entry("The Matrix", 2.5), Map.entry("Finding Nemo", 4.5), Map.entry("Spider-Man", 3.0), Map.entry("Madagascar", 2.0),
                Map.entry("Inside Out", 4.0), Map.entry("The Dark Knight", 3.0), Map.entry("La La Land", 4.5)
        ));

        userRatings.put("Dave", Map.ofEntries(
                Map.entry("Iron Man", 4.5), Map.entry("Avengers", 4.0), Map.entry("Black Panther", 5.0), Map.entry("Inception", 3.5),
                Map.entry("Joker", 4.0), Map.entry("Shrek", 4.0), Map.entry("Minions", 3.5), Map.entry("Frozen", 2.0), Map.entry("Lion King", 4.5),
                Map.entry("Toy Story", 4.0), Map.entry("Guardians of the Galaxy", 4.5), Map.entry("The Shawshank Redemption", 3.5), Map.entry("Forrest Gump", 2.0),
                Map.entry("The Matrix", 3.0), Map.entry("Finding Nemo", 3.5), Map.entry("Spider-Man", 5.0), Map.entry("Madagascar", 4.0),
                Map.entry("Inside Out", 2.5), Map.entry("The Dark Knight", 4.5), Map.entry("La La Land", 2.0)
        ));

        userRatings.put("Eve", Map.ofEntries(
                Map.entry("Coco", 5.0), Map.entry("Up", 5.0), Map.entry("Toy Story", 5.0), Map.entry("Frozen", 4.0), Map.entry("Minions", 4.5),
                Map.entry("Shrek", 4.5), Map.entry("Lion King", 5.0), Map.entry("Joker", 3.0), Map.entry("Titanic", 4.0), Map.entry("Iron Man", 4.0),
                Map.entry("Guardians of the Galaxy", 2.5), Map.entry("The Shawshank Redemption", 4.0), Map.entry("Forrest Gump", 3.5),
                Map.entry("The Matrix", 2.0), Map.entry("Finding Nemo", 4.5), Map.entry("Spider-Man", 3.5), Map.entry("Madagascar", 3.0),
                Map.entry("Inside Out", 5.0), Map.entry("The Dark Knight", 2.5), Map.entry("La La Land", 4.0)
        ));
    }

    private static List<String> getRecommendations(String targetUser) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, Double> totalSim = new HashMap<>();

        for (String otherUser : userRatings.keySet()) {
            if (otherUser.equals(targetUser)) continue;

            double similarity = pearsonSimilarity(userRatings.get(targetUser), userRatings.get(otherUser));
            if (similarity <= 0) continue;

            for (Map.Entry<String, Double> entry : userRatings.get(otherUser).entrySet()) {
                String movie = entry.getKey();

                // Ignore movies already rated by the target user
                if (userRatings.get(targetUser).containsKey(movie)) continue;

                scores.put(movie, scores.getOrDefault(movie, 0.0) + entry.getValue() * similarity);
                totalSim.put(movie, totalSim.getOrDefault(movie, 0.0) + similarity);
            }
        }

        // Normalize scores
        Map<String, Double> rankings = new HashMap<>();
        for (String movie : scores.keySet()) {
            rankings.put(movie, scores.get(movie) / totalSim.get(movie));
        }

        // Sort by highest predicted rating
        return rankings.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5) // Limit top 5 recommendations
                .toList();
    }

    private static double pearsonSimilarity(Map<String, Double> ratings1, Map<String, Double> ratings2) {
        Set<String> common = new HashSet<>(ratings1.keySet());
        common.retainAll(ratings2.keySet());

        int n = common.size();
        if (n == 0) return 0;

        double sum1 = 0, sum2 = 0;
        double sum1Sq = 0, sum2Sq = 0;
        double pSum = 0;

        for (String movie : common) {
            double r1 = ratings1.get(movie);
            double r2 = ratings2.get(movie);

            sum1 += r1;
            sum2 += r2;
            sum1Sq += r1 * r1;
            sum2Sq += r2 * r2;
            pSum += r1 * r2;
        }

        double numerator = pSum - (sum1 * sum2 / n);
        double denominator = Math.sqrt((sum1Sq - (sum1 * sum1) / n) * (sum2Sq - (sum2 * sum2) / n));

        return denominator == 0 ? 0 : numerator / denominator;
    }
}
