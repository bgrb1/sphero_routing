package sphero.algo.global.benchmarks;

import sphero.algo.MazeGenerator;
import sphero.algo.global.LocalSearch;
import sphero.algo.global.PathPermutations;
import sphero.algo.local.AllPairShortestPaths;
import sphero.common.Configuration;
import sphero.common.Path;
import sphero.common.RoutingResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LSBenchmarkTools extends LocalSearch {
    public LSBenchmarkTools(AllPairShortestPaths allPairShortestPaths, int iterations) {
        super(allPairShortestPaths, iterations);
    }

    public static void main(String[] args) throws IOException {
        printBenchmarkReport();
        /*GUI gui = new GUI("Hello Darkness my old friend");
        Configuration c = new ConfigurationBuilder(new File("resources/inputs/input_01.txt")).create();
        TwoOpt t = new TwoOpt(new AllPairShortestPaths(), 30);
        RouteObserver obs_to = t.calculate(c);
        double cost = cost(obs_to.getRoute());

        PathPermutations pm = new PathPermutations(new AllPairShortestPaths());
        RouteObserver obs_pm = pm.calculate(c);
        int opt = cost(obs_pm.getRoute());
        System.out.println(cost/opt); */
    }

    public static void printBenchmarkReport() {

        LSBenchmarkTools t = new LSBenchmarkTools(new AllPairShortestPaths(), 1000);
        double[] results = t.benchmarkOptimality(1, 10, 13);
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

        double[] time_results = t.benchmarkTime(1, 100, 100);
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

    public double[] benchmarkTime(int scenarios, int min_size, int max_size) {
        double[] results = new double[scenarios];
        Random r = new Random();
        for (int i = 0; i < scenarios; i++) {
            int size = r.nextInt(max_size - min_size + 1) + min_size;
            Configuration config = (new MazeGenerator()).generate(500, 500, size, true);
            long t1 = System.currentTimeMillis();
            calculate(config);
            long t2 = System.currentTimeMillis();
            double diff = (t2 - t1) / 1000.0;
            results[i] = diff;
        }
        return results;
    }
}
