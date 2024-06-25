package Project;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class FloodReliefImpl extends Application {
    private int V; // Number of locations
    private List<String> nodeNames; // Names of the locations
    private List<int[]> edges; // Edges between locations with distances
    private int[] supplies; // Supplies at each location
    private int[] demands; // Demands at each location

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox inputBox = new VBox(10); // VBox layout for input fields
        inputBox.setPadding(new Insets(10));

        // Input fields
        TextField numNodesField = new TextField();
        numNodesField.setPromptText("Enter number of locations");

        TextArea nodesArea = new TextArea();
        nodesArea.setPromptText("Enter location names (one per line)");

        TextArea edgesArea = new TextArea();
        edgesArea.setPromptText("Enter edges in format 'node1 node2 distance' (one per line)");

        TextArea suppliesArea = new TextArea();
        suppliesArea.setPromptText("Enter supplies at each location (one per line)");

        TextArea demandsArea = new TextArea();
        demandsArea.setPromptText("Enter demands at each location (one per line)");

        Button submitButton = new Button("Submit");

        // Event handler for submit button
        submitButton.setOnAction(e -> {
            try {
                // Parse the number of locations
                V = Integer.parseInt(numNodesField.getText().trim());
                if (V <= 0) throw new NumberFormatException();

                nodeNames = new ArrayList<>();
                edges = new ArrayList<>();
                supplies = new int[V];
                demands = new int[V];

                // Parse location names
                String[] nodes = nodesArea.getText().trim().split("\\n");
                if (nodes.length != V) throw new Exception("Number of nodes does not match the specified count.");

                for (String node : nodes) {
                    nodeNames.add(node.trim());
                }

                // Parse edges
                String[] edgesInput = edgesArea.getText().trim().split("\\n");
                for (String edge : edgesInput) {
                    String[] parts = edge.trim().split("\\s+");
                    if (parts.length != 3) throw new Exception("Invalid edge format.");
                    int node1Index = nodeNames.indexOf(parts[0]);
                    int node2Index = nodeNames.indexOf(parts[1]);
                    if (node1Index == -1 || node2Index == -1) throw new Exception("Invalid node names in edges.");
                    edges.add(new int[]{node1Index, node2Index, Integer.parseInt(parts[2])});
                }

                // Parse supplies
                String[] suppliesInput = suppliesArea.getText().trim().split("\\n");
                if (suppliesInput.length != V) throw new Exception("Number of supplies entries does not match the specified count.");
                for (int i = 0; i < suppliesInput.length; i++) {
                    supplies[i] = Integer.parseInt(suppliesInput[i].trim());
                }

                // Parse demands
                String[] demandsInput = demandsArea.getText().trim().split("\\n");
                if (demandsInput.length != V) throw new Exception("Number of demands entries does not match the specified count.");
                for (int i = 0; i < demandsInput.length; i++) {
                    demands[i] = Integer.parseInt(demandsInput[i].trim());
                }

                // Display the graph
                showGraph(primaryStage);
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid number format. Please check your data and try again.");
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
        });

        // Add input fields to the layout
        inputBox.getChildren().addAll(
                new Label("Number of Locations:"),
                numNodesField,
                new Label("Location Names:"),
                nodesArea,
                new Label("Edges (format: node1 node2 distance):"),
                edgesArea,
                new Label("Supplies at each location:"),
                suppliesArea,
                new Label("Demands at each location:"),
                demandsArea,
                submitButton
        );

        // Set up the initial scene with input fields
        Scene inputScene = new Scene(inputBox, 400, 600);
        primaryStage.setTitle("Flood Relief Optimization Input");
        primaryStage.setScene(inputScene);
        primaryStage.show();
    }

    // Method to display the graph and supply distribution
    private void showGraph(Stage primaryStage) {
        // Create the graph
        Graph graph = new Graph(V);
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1], edge[2]);
        }

        // Calculate shortest paths between all pairs of nodes
        int[][] shortestPaths = new int[V][V];
        for (int i = 0; i < V; i++) {
            shortestPaths[i] = graph.dijkstra(i);
        }

        // StringBuilder to log the distribution messages
        StringBuilder distributionLog = new StringBuilder();
        SupplyDistribution supplyDistribution = new SupplyDistribution(V, supplies, demands, shortestPaths, distributionLog, nodeNames);
        supplyDistribution.distributeSupplies();

        // JavaFX setup to draw the graph and show distribution messages
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawGraph(gc, V, graph, shortestPaths);

        // Display the distribution messages in a TextArea
        TextArea outputArea = new TextArea(distributionLog.toString());
        outputArea.setEditable(false);
        outputArea.setPrefHeight(200);

        VBox vbox = new VBox(canvas, outputArea);
        Scene scene = new Scene(vbox, 800, 800);

        primaryStage.setTitle("Flood Relief Optimization Output");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Method to draw the graph on the canvas
    private void drawGraph(GraphicsContext gc, int V, Graph graph, int[][] shortestPaths) {
        // Positions for nodes
        double[][] positions = new double[V][2];
        for (int i = 0; i < V; i++) {
            positions[i][0] = 100 + (i % 3) * 200;
            positions[i][1] = 100 + (i / 3) * 200;
        }

        // Draw nodes
        for (int i = 0; i < V; i++) {
            gc.setFill(Color.BLUE);
            gc.fillOval(positions[i][0], positions[i][1], 20, 20);
            gc.setFill(Color.BLACK);
            gc.fillText(nodeNames.get(i), positions[i][0], positions[i][1] - 10);
        }

        // Draw edges
        gc.setStroke(Color.BLACK);
        for (int u = 0; u < V; u++) {
            for (Graph.Node neighbor : graph.getAdj().get(u)) {
                int v = neighbor.node;
                gc.strokeLine(positions[u][0] + 10, positions[u][1] + 10, positions[v][0] + 10, positions[v][1] + 10);
                double midX = (positions[u][0] + positions[v][0]) / 2;
                double midY = (positions[u][1] + positions[v][1]) / 2;
                gc.fillText(String.valueOf(neighbor.cost), midX, midY);
            }
        }

        // Draw shortest paths (just an example, you may need to adjust)
        gc.setStroke(Color.RED);
        for (int u = 0; u < V; u++) {
            for (int v = 0; v < V; v++) {
                if (u != v && shortestPaths[u][v] < Integer.MAX_VALUE) {
                    gc.strokeLine(positions[u][0] + 10, positions[u][1] + 10, positions[v][0] + 10, positions[v][1] + 10);
                }
            }
        }
    }

    // Method to show alert dialogs
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
