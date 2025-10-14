package minerful.dfg.encdec;

import minerful.dfg.DirectlyFollowsGraph;

import java.io.*;
import java.util.*;

public class DfgExporter {

    public static void exportToDfg(DirectlyFollowsGraph graph, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath)))) {
            writeGraph(graph, writer);
        }
    }

    public static String exportToDfg(DirectlyFollowsGraph graph) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (BufferedWriter writer = new BufferedWriter(stringWriter)) {
            writeGraph(graph, writer);
        }
        return stringWriter.toString();
    }

    private static void writeGraph(DirectlyFollowsGraph graph, BufferedWriter writer) throws IOException {
        List<DirectlyFollowsGraph.ActNode> nodes = new ArrayList<>(graph.getNodes().values());

        writer.write(String.valueOf(nodes.size()));
        writer.newLine();
        for (DirectlyFollowsGraph.ActNode node : nodes) {
            writer.write(node.getName());
            writer.newLine();
        }

        List<DirectlyFollowsGraph.ActNode> startNodes = new ArrayList<>();
        Map<DirectlyFollowsGraph.ActNode, Long> startFreqs = new HashMap<>();
        for (DirectlyFollowsGraph.ActNode node : nodes) {
            for (DirectlyFollowsGraph.Arc arc : node.getInArcs()) {
                if ("INIT".equals(arc.getFrom().getName())) {
                    startNodes.add(node);
                    startFreqs.put(node, arc.getOccurrences());
                }
            }
        }

        writer.write(String.valueOf(startNodes.size()));
        writer.newLine();
        for (DirectlyFollowsGraph.ActNode node : startNodes) {
            int idx = nodes.indexOf(node);
            writer.write(idx + "x" + startFreqs.get(node));
            writer.newLine();
        }

        List<DirectlyFollowsGraph.ActNode> endNodes = new ArrayList<>();
        Map<DirectlyFollowsGraph.ActNode, Long> endFreqs = new HashMap<>();
        for (DirectlyFollowsGraph.ActNode node : nodes) {
            for (DirectlyFollowsGraph.Arc arc : node.getOutArcs()) {
                if ("END".equals(arc.getTo().getName())) {
                    endNodes.add(node);
                    endFreqs.put(node, arc.getOccurrences());
                }
            }
        }

        writer.write(String.valueOf(endNodes.size()));
        writer.newLine();
        for (DirectlyFollowsGraph.ActNode node : endNodes) {
            int idx = nodes.indexOf(node);
            writer.write(idx + "x" + endFreqs.get(node));
            writer.newLine();
        }

        for (DirectlyFollowsGraph.Arc arc : graph.getArcs()) {
            if ("INIT".equals(arc.getFrom().getName()) || "END".equals(arc.getTo().getName())) {
                continue; 
            }
            int fromIdx = nodes.indexOf(arc.getFrom());
            int toIdx = nodes.indexOf(arc.getTo());
            writer.write(fromIdx + ">" + toIdx + "x" + arc.getOccurrences());
            writer.newLine();
        }

        writer.flush();
    }
}
