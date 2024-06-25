package Project;

import java.util.*;

public class SupplyDistribution {
	private final int V;
	private final int[] supplies;
	private final int[] demands;
	private final int[][] shortestPaths;
	private final StringBuilder distributionLog; // To store the distribution messages
	private final List<String> nodeNames; // List of location names

	public SupplyDistribution(int V, int[] supplies, int[] demands, int[][] shortestPaths,
			StringBuilder distributionLog, List<String> nodeNames) {
		this.V = V;
		this.supplies = supplies;
		this.demands = demands;
		this.shortestPaths = shortestPaths;
		this.distributionLog = distributionLog; // Initialize the log
		this.nodeNames = nodeNames; // Initialize the list of location names
	}

	public void distributeSupplies() {
		PriorityQueue<Location> pq = new PriorityQueue<>(Comparator.comparingInt(Location::getPriority));
		for (int i = 0; i < V; i++) {
			pq.add(new Location(i, demands[i]));
		}

		while (!pq.isEmpty() && Arrays.stream(supplies).sum() > 0) {
			Location loc = pq.poll();
			int location = loc.getLocation();
			int demand = loc.getDemand();
			int supplied = 0;

			for (int i = 0; i < V; i++) {
				if (supplies[i] > 0) {
					int canSupply = Math.min(supplies[i], demand - supplied);
					supplied += canSupply;
					supplies[i] -= canSupply;
					String message = "Supplied " + canSupply + " units from " + nodeNames.get(i) + " to "
							+ nodeNames.get(location) + " via distance " + shortestPaths[i][location];
					distributionLog.append(message).append("\n"); // Append message to the log
					System.out.println(message);
					if (supplied == demand)
						break;
				}
			}

			if (supplied < demand) {
				pq.add(new Location(location, demand - supplied));
			}
		}
	}

	static class Location {
		private final int location;
		private final int demand;

		public Location(int location, int demand) {
			this.location = location;
			this.demand = demand;
		}

		public int getLocation() {
			return location;
		}

		public int getDemand() {
			return demand;
		}

		public int getPriority() {
			return demand; // Priority can be adjusted based on additional criteria
		}
	}
}
