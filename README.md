This repository contains some parts of a university project from the course "Algorithms and Data Structures: Programming Practice" (spring 2021).
The goal for this component was to develop a routing algorithm for a robot (SpheroBolt) in a maze with multiple destinations that need to be visited.

The algorithm implementation consists of two main components:
1) Local Routing: Find the shortest paths between all destination pairs, in our case using the A* algorithm
2) Global Routing: Using the costs from the local routing, find the optimal order in which to visit the destinations. This equivalent
to the NP-hard Traveling Salesman Problem. To solve the problem we went for heuristic algorithms, which scientific research has shown
to be time-efficient and often lead to better solutions than approximation algorithms (like the algorithm of Christofides).
More specifically, we implemented both a Local Search solution, and one using an Ant Colony heuristic, with the first one showing better results.
Since we had a lot more compute resources available than needed, we performed Local Search with many random starting positions (parallelized)
and then chose the best solution. 

My contribution was the global routing (see src/sphero/algo/global), as well as parts of the OO modeling (see src/sphero/common)