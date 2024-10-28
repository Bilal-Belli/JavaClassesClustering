package step2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class ClusteringAlgorithmConditione {
	private Graph<String, DefaultWeightedEdge> graphCouplage;
    private float CP;

    // Constructor
    public ClusteringAlgorithmConditione(Graph<String, DefaultWeightedEdge> graphCouplage, float parametreCondition) {
        this.CP = parametreCondition;
        this.graphCouplage = graphCouplage;
    }

    // Main function to execute the clustering algorithm
    public void clusterClasses() {
        // Step 1: Initialize each class as its own cluster
        List<Set<String>> clusters = initializeClusters();
        int cmpt = 0;
        int totalClasses = clusters.size();
        
        // Step 2: Loop until we reach the target number of clusters or the conditions are met
        while (true) {
            // Step 3: Find the most coupled clusters
            int[] pairToMerge = findMostCoupledClusters(clusters);
            int clusterAIndex = pairToMerge[0];
            int clusterBIndex = pairToMerge[1];

            // Step 4: Calculate the average internal coupling of each cluster
            boolean stop = false;    
           	for (Set<String> cluster : clusters) {
           		List<String> classListForSize = new ArrayList<>(cluster);
           		if (classListForSize.size()==1) {
           			// ne comptez pas les clusters qui ne contiennent qu'une seule classe, car au début chaque classe est un cluster
            		continue;
            	} else {
            		double averageCoupling = calculateInternalAverageCoupling(cluster);
                    // System.out.println(averageCoupling);
                    // Condition 2: If the internal average coupling of any cluster is greater than CP, stop
                    if (averageCoupling < CP) {
                        System.out.println("Condition 2 met: Internal average coupling must be >= CP in a cluster. Stopping...");
                        stop = true;
                        break;
                    }
            	}
            }
           	
            if (stop) {
                break;
            }

            // Optional: Print clusters after each merge
            System.out.println("--------------------------------");
            System.out.println("Merged clusters [step: " + cmpt + "]:");
            System.out.println("--------------------------------");
            printClusters(clusters);
            
            // Condition 1: If clusters are less than or equal to half the total number of classes, stop
            if (clusters.size() <= totalClasses / 2) {
                System.out.println("Condition 1 met: Number of clusters <= (total classes / 2). Stopping...");
                break;
            }
            // Step 5: Merge the two most coupled clusters
            mergeClusters(clusters, clusterAIndex, clusterBIndex);
            cmpt++;   
            }
    }

    // Initialize each class as its own cluster
    private List<Set<String>> initializeClusters() {
        List<Set<String>> clusters = new ArrayList<>();
        for (String className : graphCouplage.vertexSet()) {
            Set<String> cluster = new HashSet<>();
            cluster.add(className);
            clusters.add(cluster);
        }
        return clusters;
    }

    // Find the two clusters with the highest coupling
    private int[] findMostCoupledClusters(List<Set<String>> clusters) {
        double maxCoupling = -1;
        int clusterAIndex = -1;
        int clusterBIndex = -1;

        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                double coupling = calculateCoupling(clusters.get(i), clusters.get(j));
                if (coupling > maxCoupling) {
                    maxCoupling = coupling;
                    clusterAIndex = i;
                    clusterBIndex = j;
                }
            }
        }

        return new int[]{clusterAIndex, clusterBIndex};
    }

    // Calculate the coupling between two clusters
    private double calculateCoupling(Set<String> clusterA, Set<String> clusterB) {
        double totalCoupling = 0.0;

        for (String classA : clusterA) {
            for (String classB : clusterB) {
                DefaultWeightedEdge edge = graphCouplage.getEdge(classA, classB);
                if (edge != null) {
                    totalCoupling += graphCouplage.getEdgeWeight(edge);
                }

                edge = graphCouplage.getEdge(classB, classA);
                if (edge != null) {
                    totalCoupling += graphCouplage.getEdgeWeight(edge);
                }
            }
        }

        return totalCoupling;
    }

    // Merge two clusters
    private void mergeClusters(List<Set<String>> clusters, int clusterAIndex, int clusterBIndex) {
        Set<String> clusterA = clusters.get(clusterAIndex);
        Set<String> clusterB = clusters.get(clusterBIndex);

        // Merge cluster B into cluster A
        clusterA.addAll(clusterB);

        // Remove cluster B from the list of clusters
        clusters.remove(clusterBIndex);
    }

    // Calculate the internal average coupling of a cluster
    private double calculateInternalAverageCoupling(Set<String> cluster) {
        double totalCoupling = 0.0;
        int numCouples = 0;

        List<String> classList = new ArrayList<>(cluster);

        for (int i = 0; i < classList.size(); i++) {
            for (int j = i + 1; j < classList.size(); j++) {
                String classA = classList.get(i);
                String classB = classList.get(j);

                DefaultWeightedEdge edge = graphCouplage.getEdge(classA, classB);
                if (edge != null) {
                    totalCoupling += graphCouplage.getEdgeWeight(edge);
                    numCouples++;
                }

                edge = graphCouplage.getEdge(classB, classA);
                if (edge != null) {
                    totalCoupling += graphCouplage.getEdgeWeight(edge);
                    numCouples++;
                }
            }
        }
        // System.out.println(classList);
        // System.out.println(numCouples);
        // System.out.println(totalCoupling);
        // Return the average coupling within the cluster
        return numCouples == 0 ? 0 : totalCoupling / numCouples;
    }

    // Print the current clusters
    private void printClusters(List<Set<String>> clusters) {
        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster " + i + ": " + clusters.get(i));
        }
    }

    
    // Example
    //public static void main(String[] args) {
        // Example setup: create a graph and perform clustering
        //Graph<String, DefaultWeightedEdge> graphCouplage = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        // Add vertices (classes)
        //graphCouplage.addVertex("ClassA");
        //graphCouplage.addVertex("ClassB");
        //graphCouplage.addVertex("ClassC");
    	//graphCouplage.addVertex("ClassD");
        
    	//graphCouplage.addVertex("ClassE");
    	//graphCouplage.addVertex("ClassF");
    	//graphCouplage.addVertex("ClassG");
    	//DefaultWeightedEdge edgeEF = graphCouplage.addEdge("ClassE", "ClassF");
    	//graphCouplage.setEdgeWeight(edgeEF, 2.0);
    	//DefaultWeightedEdge edgeFG = graphCouplage.addEdge("ClassF", "ClassG");
    	//graphCouplage.setEdgeWeight(edgeFG, 2.0);
    	//DefaultWeightedEdge edgeEG = graphCouplage.addEdge("ClassE", "ClassG");
    	//graphCouplage.setEdgeWeight(edgeEG, 2.0);
    	//DefaultWeightedEdge edgeEC = graphCouplage.addEdge("ClassE", "ClassC");
    	//graphCouplage.setEdgeWeight(edgeEC, 1.0);

        // Add edges with weights (coupling)
    	//DefaultWeightedEdge edgeAB = graphCouplage.addEdge("ClassA", "ClassB");
    	//graphCouplage.setEdgeWeight(edgeAB, 5.0);

    	//DefaultWeightedEdge edgeAC = graphCouplage.addEdge("ClassA", "ClassC");
        //graphCouplage.setEdgeWeight(edgeAC, 2.0);

    	//DefaultWeightedEdge edgeBD = graphCouplage.addEdge("ClassB", "ClassC");
        //graphCouplage.setEdgeWeight(edgeBD, 1.0);

    	//DefaultWeightedEdge edgeCD = graphCouplage.addEdge("ClassC", "ClassD");
        //graphCouplage.setEdgeWeight(edgeCD, 4.0);

        // Perform clustering
    	//ClusteringAlgorithmConditione clustering = new ClusteringAlgorithmConditione(graphCouplage, 2f); // CP = 
    	//clustering.clusterClasses();  // Set target clusters to 1
        // uncomment line 48 to print average and track it
    //}
}
