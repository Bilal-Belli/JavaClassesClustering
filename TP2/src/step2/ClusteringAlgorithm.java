package step2;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import java.util.*;

public class ClusteringAlgorithm {
    private Graph<String, DefaultWeightedEdge> graphCouplage;
    
    // Constructor
    public ClusteringAlgorithm(Graph<String, DefaultWeightedEdge> graphCouplage) {
        this.graphCouplage = graphCouplage;
    }

    // Main function to execute the clustering algorithm
    public void clusterClasses(int targetClusterCount) {
        // Step 1: Initialize each class as its own cluster
        List<Set<String>> clusters = initializeClusters();
        int cmpt=0;
        // Step 2: Loop until we reach the target number of clusters
        while (clusters.size() > targetClusterCount) {
            // Step 3: Find the most coupled clusters
            int[] pairToMerge = findMostCoupledClusters(clusters);
            int clusterAIndex = pairToMerge[0];
            int clusterBIndex = pairToMerge[1];
            
            // Step 4: Merge the two most coupled clusters
            mergeClusters(clusters, clusterAIndex, clusterBIndex);
            
            // Optional: Print clusters after each merge
            System.out.println("--------------------------------");
            System.out.println("Merged clusters [step: "+cmpt+"]:");
            System.out.println("--------------------------------");
            printClusters(clusters);
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

    // Print the current clusters
    private void printClusters(List<Set<String>> clusters) {
        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster " + i + ": " + clusters.get(i));
        }
    }

    public static void main(String[] args) {
        // Example setup: create a graph and perform clustering
        Graph<String, DefaultWeightedEdge> graphCouplage = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        
        // Add vertices (classes)
        graphCouplage.addVertex("ClassA");
        graphCouplage.addVertex("ClassB");
        graphCouplage.addVertex("ClassC");
        graphCouplage.addVertex("ClassD");

        // Add edges with weights (coupling)
        DefaultWeightedEdge edgeAB = graphCouplage.addEdge("ClassA", "ClassB");
        graphCouplage.setEdgeWeight(edgeAB, 5.0);

        DefaultWeightedEdge edgeAC = graphCouplage.addEdge("ClassA", "ClassC");
        graphCouplage.setEdgeWeight(edgeAC, 2.0);

        DefaultWeightedEdge edgeBD = graphCouplage.addEdge("ClassB", "ClassC");
        graphCouplage.setEdgeWeight(edgeBD, 1.0);

        DefaultWeightedEdge edgeCD = graphCouplage.addEdge("ClassC", "ClassD");
        graphCouplage.setEdgeWeight(edgeCD, 4.0);

        // Perform clustering
        ClusteringAlgorithm clustering = new ClusteringAlgorithm(graphCouplage);
        clustering.clusterClasses(2);  // Set target clusters to 2
    }
}