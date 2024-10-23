package sphero.algo.global.benchmarks;

import sphero.algo.MazeGenerator;
import sphero.algo.global.AntColony;
import sphero.algo.global.LocalSearch_TimeCritical;
import sphero.algo.global.PathPermutations;
import sphero.algo.local.AllPairShortestPaths;
import sphero.common.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LSTCBenchmarkTools extends LocalSearch_TimeCritical {
    public LSTCBenchmarkTools(AllPairShortestPaths allPairShortestPaths, int iterations) {
        super(allPairShortestPaths, iterations);
    }

    public static void main(String[] args) throws IOException {
        // printBenchmarkReport();
        LSTCBenchmarkTools t = new LSTCBenchmarkTools(new AllPairShortestPaths(), 1);
        t.testCircle(5);
        t.benchmarkDeadlines_random(10, 10, 12);
        System.out.println("---------------");
        t.benchmarkDeadlines_circular(10, 10, 12);

    }

    public static void printBenchmarkReport() {

        LSTCBenchmarkTools t = new LSTCBenchmarkTools(new AllPairShortestPaths(), 1000);
        double[] results = t.benchmarkOptimality(10, 10, 13);
        double best = Arrays.stream(results).min().getAsDouble();
        double avrg = Arrays.stream(results).sum() / results.length;
        double worst = Arrays.stream(results).max().getAsDouble();
        double opt = 100.0 * Arrays.stream(results).filter(d -> d == 1.0).count() / results.length;
        System.out.println("------Optimality---------");
        System.out.println("Best  = " + best);
        System.out.println("Avrg  = " + avrg);
        System.out.println("Worst = " + worst);
        System.out.println("Opt-Ratio  = " + opt + "%");
        System.out.println("-------------------------");

        double[] time_results = t.benchmarkTime(1, 20, 25);
        double best_t = Arrays.stream(time_results).min().getAsDouble();
        double avrg_t = Arrays.stream(time_results).sum() / time_results.length;
        double worst_t = Arrays.stream(time_results).max().getAsDouble();
        System.out.println("----------Time-----------");
        System.out.println("Best  = " + best_t);
        System.out.println("Avrg  = " + avrg_t);
        System.out.println("Worst = " + worst_t);
        System.out.println("-------------------------");
    }

    public double[] benchmarkOptimality(int scenarios, int min_size, int max_size) {
        double[] results = new double[scenarios];
        Random r = new Random();
        for (int i = 0; i < scenarios; i++) {
            int size = r.nextInt(max_size - min_size + 1) + min_size;
            Configuration config = (new MazeGenerator()).generate(50, 50, size, true);
            List<Path> solution = calculate(config).getRoute();
            double cost = cost(solution);

            PathPermutations pm = new PathPermutations(allPairShortestPaths);
            RoutingResult obs = pm.calculate(config);
            double opt = cost(obs.getRoute());

            results[i] = cost / opt;
        }
        return results;
    }

    public void testCircle(int n) {
        Map m = new Map(100, 100, 1, false);
        List<Field> fields = new ArrayList<>();
        fields.add(m.getField(50, 95));
        fields.add(m.getField(70, 90));
        fields.add(m.getField(90, 70));
        fields.add(m.getField(95, 50));
        fields.add(m.getField(90, 30));
        fields.add(m.getField(70, 10));
        fields.add(m.getField(50, 5));
        fields.add(m.getField(30, 10));
        fields.add(m.getField(10, 30));
        fields.add(m.getField(5, 50));
        fields.add(m.getField(10, 70));
        fields.add(m.getField(30, 90));

        List<Location> locations = new ArrayList<>();
        locations.add(new Location(fields.get(0), Location.Type.START));
        double time = 0;
        double opt = 0;
        for (int i = 1; i < fields.size(); i++) {
            Field from = fields.get(i - 1);
            Field to = fields.get(i);
            double arrival = time + Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY()) + 5;
            opt += Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY());
            locations.add(new Location(to, Location.Type.DESTINATION, arrival));
            time = arrival;
        }
        Configuration c = (new ConfigurationBuilder()).withMap(m).withStart(locations.get(0)).withLocations(locations).create();
        Field from = fields.get(locations.size() - 1);
        Field to = fields.get(0);
        opt += Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY());

        List<Path> solution_local = calculate(c).getRoute();
        RoutingResult solution_ants = (new AntColony(allPairShortestPaths, 10000,16)).calculate(c);

        double cost_local = cost(solution_local);
        double cost_ants = solution_ants.getCost();

        System.out.println("Local: " + cost_local + "  |  " + cost_local/opt);
        System.out.println("Ants: " + cost_ants+ "  |  " + cost_ants/opt);
    }

    public double[] benchmarkTime(int scenarios, int min_size, int max_size) {
        double[] results = new double[scenarios];
        Random r = new Random();
        for (int i = 0; i < scenarios; i++) {
            int size = r.nextInt(max_size - min_size + 1) + min_size;
            Configuration config = (new MazeGenerator()).generate(50, 50, size, true);
            long t1 = System.currentTimeMillis();
            calculate(config);
            long t2 = System.currentTimeMillis();
            double diff = (t2 - t1) / 1000.0;
            results[i] = diff;
        }
        return results;
    }

    public boolean[] benchmarkDeadlines_random(int scenarios, int min_size, int max_size) {
        boolean[] results = new boolean[scenarios];
        Random r = new Random();
        for (int i = 0; i < scenarios; i++) {
            int size = r.nextInt(max_size - min_size + 1) + min_size;
            Configuration config = (new MazeGenerator()).generate(50, 50, size, false);
            Location[] deadline_order = randomOrder(config);
            double test_dist = setDeadlines(allPairShortestPaths.allPairs(config.getOrderedLocations(), config.getMap()), deadline_order, 1.5);
            List<Path> route = calculate(config).getRoute();
            int count = violatedDeadlines(route);
            System.out.println("Violated: " + count + "   |   Ratio: " + cost(route) / test_dist);
        }
        return results;
    }

    public boolean[] benchmarkDeadlines_circular(int scenarios, int min_size, int max_size) {
        boolean[] results = new boolean[scenarios];
        Random r = new Random();
        for (int i = 0; i < scenarios; i++) {
            int size = r.nextInt(max_size - min_size + 1) + min_size;
            Configuration config = (new MazeGenerator()).generateCircular(50, 50, size, true);
            Location[] deadline_order = new Location[config.getOrderedLocations().size()];
            config.getOrderedLocations().toArray(deadline_order);
            double test_dist = setDeadlines(allPairShortestPaths.allPairs(config.getOrderedLocations(), config.getMap()), deadline_order, 1.5);
            List<Path> route = calculate(config).getRoute();
            int count = violatedDeadlines(route);
            System.out.println("Violated: " + count + "   |   Ratio: " + cost(route) / test_dist);
        }
        return results;
    }

    public double setDeadlines(Path[][] matrix, Location[] tour, double looseness) {
        double time = 0;
        double dist = 0;
        for (int i = 1; i < tour.length; i++) {
            Location from = tour[i - 1];
            Location to = tour[i];
            time += matrix[from.getId()][to.getId()].getTravelTime() * looseness;
            dist += matrix[from.getId()][to.getId()].getDistance();
            to.setDeadline(time);
        }
        dist += matrix[tour[tour.length - 1].getId()][tour[0].getId()].getDistance();
        return dist;
    }
}
