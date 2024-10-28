package step2;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class Parser {
    public static final String projectPath = "C:\\Users\\Hp\\OneDrive\\Bureau\\913 EVLP\\TP1P2\\ProjectsToParse\\PipeFilterCalculator";
    public static final String projectSourcePath = projectPath + "\\src";
    public static final String jrePath = "C:\\Program Files\\Java\\jre1.8.0_51\\lib\\rt.jar";
    
    private static Graph<String, DefaultEdge> callGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
    private static Graph<String, DefaultWeightedEdge> graphCouplage = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    private static Map<String, Map<String, Integer>> classCallCounts_qst1 = new HashMap<>();
    private static Map<String, Map<String, Integer>> classCallCounts_qst2 = new HashMap<>();
    
    private static int totalMethodCalls = 0;
    
    public static void main(String[] args) throws IOException {
        final File folder = new File(projectSourcePath);
        ArrayList<File> javaFiles = listJavaFilesForFolder(folder);

        for (File fileEntry : javaFiles) {
            String content = FileUtils.readFileToString(fileEntry);
            CompilationUnit parse = parse(content.toCharArray());
            buildCallGraph(parse);
            buildCouplingGraph(parse);
        }
              
        // Exercice 1 Question 1
        // Calculate and print calls for each class pair
        System.out.println("--------------------------------------");
        System.out.println("Métrique de couplage entre les classes");
        System.out.println("--------------------------------------");
        for (String callerClass : classCallCounts_qst1.keySet()) {
            Map<String, Integer> calleeCounts = classCallCounts_qst1.get(callerClass);
            for (String calleeClass : calleeCounts.keySet()) {
                int callsBetween = calleeCounts.get(calleeClass);
                double coupling = (double) callsBetween / totalMethodCalls;
                // System.out.println(callsBetween);
                // System.out.println(totalMethodCalls);
                System.out.printf("Couplage(%s, %s) = %.2f\n", callerClass, calleeClass, coupling);
            }
        }
        
        // Exercice 1 Question 2
        System.out.println("------------------------------");
        System.out.println("Graphe de couplage des classes");
        System.out.println("------------------------------");
        // Print the weighted coupling graph
        for (DefaultWeightedEdge edge : graphCouplage.edgeSet()) {
            String source = graphCouplage.getEdgeSource(edge);
            String target = graphCouplage.getEdgeTarget(edge);
            double weight = graphCouplage.getEdgeWeight(edge);
            System.out.printf("Class %s -> Class %s, poid (Appels) = %.0f\n", source, target, weight);
        } 
        
        // Exercice 2 Question 1
        // Clustering Algorithm (donne a la fin un seul cluster qui regroupe tous les classes)
        ClusteringAlgorithm clustering = new ClusteringAlgorithm(graphCouplage);
        clustering.clusterClasses(1);
        
        // Exercice 2 Question 2
        System.out.println("------------------------------------------------------------");
        System.out.println("Groupes des classes selon les conditions et le parametre 'CP'");
        System.out.println("------------------------------------------------------------");
        int CP=3;
        ClusteringAlgorithmConditione clustersExo2Qst2 = new ClusteringAlgorithmConditione(graphCouplage,CP);
        clustersExo2Qst2.clusterClasses();
        
        // Show the weighted coupling graph in a window
        showGraph(graphCouplage);
    }

    public static ArrayList<File> listJavaFilesForFolder(final File folder) {
        ArrayList<File> javaFiles = new ArrayList<>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                javaFiles.addAll(listJavaFilesForFolder(fileEntry));
            } else if (fileEntry.getName().endsWith(".java")) {
                javaFiles.add(fileEntry);
            }
        }
        return javaFiles;
    }

    private static CompilationUnit parse(char[] classSource) {
        ASTParser parser = ASTParser.newParser(AST.JLS4); // Java 1.6+
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
        Map<?, ?> options = JavaCore.getOptions();
        parser.setCompilerOptions(options);
        parser.setUnitName("");
        String[] sources = { projectSourcePath };
        String[] classpath = { jrePath };
        parser.setEnvironment(classpath, sources, new String[] { "UTF-8" }, true);
        parser.setSource(classSource);
        return (CompilationUnit) parser.createAST(null); // create and parse
    }

    // Qst 1: Build call graph with class associations
    public static void buildCallGraph(CompilationUnit parse) {
        TypeDeclarationVisitor typeVisitor = new TypeDeclarationVisitor();
        parse.accept(typeVisitor);
        for (TypeDeclaration type : typeVisitor.getTypes()) {
            String className = type.getName().toString(); // Get the class name
            MethodDeclarationVisitor methodVisitor = new MethodDeclarationVisitor();
            type.accept(methodVisitor);
            for (MethodDeclaration method : methodVisitor.getMethods()) {
                MethodInvocationVisitor invocationVisitor = new MethodInvocationVisitor();
                method.accept(invocationVisitor);
                String caller = className + "." + method.getName().toString(); // Associate method with class
                for (MethodInvocation methodInvocation : invocationVisitor.getMethods()) {
                    // Resolve method binding and ensure it's not null
                    if (methodInvocation.resolveMethodBinding() != null && methodInvocation.resolveMethodBinding().getDeclaringClass() != null) {
                        String calleeClass = methodInvocation.resolveMethodBinding().getDeclaringClass().getName();
                        String callee = calleeClass + "." + methodInvocation.getName().toString(); // Associate callee method with its class
                        callGraph.addVertex(caller);
                        callGraph.addVertex(callee);
                        callGraph.addEdge(caller, callee);
                        totalMethodCalls++;
                        // Update class call counts
                        classCallCounts_qst1.computeIfAbsent(className, k -> new HashMap<>()).put(calleeClass, classCallCounts_qst1.get(className).getOrDefault(calleeClass, 0) + 1);
                    }
                }
            }
        }
    }
    
    // Qst 2: Build call graph with class associations and weights
    public static void buildCouplingGraph(CompilationUnit parse) {
        TypeDeclarationVisitor typeVisitor = new TypeDeclarationVisitor();
        parse.accept(typeVisitor);
        for (TypeDeclaration type : typeVisitor.getTypes()) {
            String className = type.getName().toString(); // Get the class name
            MethodDeclarationVisitor methodVisitor = new MethodDeclarationVisitor();
            type.accept(methodVisitor);
            for (MethodDeclaration method : methodVisitor.getMethods()) {
                MethodInvocationVisitor invocationVisitor = new MethodInvocationVisitor();
                method.accept(invocationVisitor);
                for (MethodInvocation methodInvocation : invocationVisitor.getMethods()) {
                    if (methodInvocation.resolveMethodBinding() != null && methodInvocation.resolveMethodBinding().getDeclaringClass() != null) {
                        String calleeClass = methodInvocation.resolveMethodBinding().getDeclaringClass().getName();
                        classCallCounts_qst2.computeIfAbsent(className, k -> new HashMap<>()).put(calleeClass, classCallCounts_qst2.get(className).getOrDefault(calleeClass, 0) + 1);
                        graphCouplage.addVertex(className);
                        graphCouplage.addVertex(calleeClass);
                        DefaultWeightedEdge edge = graphCouplage.getEdge(className, calleeClass);
                        if (edge == null) {
                            edge = graphCouplage.addEdge(className, calleeClass);
                            graphCouplage.setEdgeWeight(edge, 1);
                        } else {
                            double currentWeight = graphCouplage.getEdgeWeight(edge);
                            graphCouplage.setEdgeWeight(edge, currentWeight + 1);
                        }
                    }
                }
            }
        }
    }

    public static void showGraph(Graph<String, DefaultWeightedEdge> graph) {
        JFrame frame = new JFrame("Weighted Call Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        
        mxGraph jGraphX = new mxGraph();
        Object parent = jGraphX.getDefaultParent();

        jGraphX.getModel().beginUpdate();
        try {
            Map<String, Object> vertexMap = new HashMap<>();
            for (String vertex : graph.vertexSet()) {
                Object v = jGraphX.insertVertex(parent, null, vertex, 100, 100, 80, 30);
                vertexMap.put(vertex, v);
            }
            for (DefaultWeightedEdge edge : graph.edgeSet()) {
                String source = graph.getEdgeSource(edge);
                String target = graph.getEdgeTarget(edge);
                double weight = graph.getEdgeWeight(edge);
                jGraphX.insertEdge(parent, null, String.format("%.0f", weight), vertexMap.get(source), vertexMap.get(target));
            }
        } finally {
            jGraphX.getModel().endUpdate();
        }

        mxGraphComponent graphComponent = new mxGraphComponent(jGraphX);
        mxCircleLayout layout = new mxCircleLayout(jGraphX);
        layout.execute(jGraphX.getDefaultParent());

        frame.add(graphComponent);
        frame.setVisible(true);
    }
}
