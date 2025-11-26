package minerful.prombridge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.*;


import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiner;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParametersIMcd;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParametersIMd;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParametersIMfd;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.DfgImportPlugin;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.plugins.EfficientTreeExportPlugin;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2AcceptingPetriNet;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2HumanReadableString;
import org.processmining.plugins.InductiveMiner.efficienttree.ProcessTree2EfficientTree;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;


public class RunInductiveMinerDirectlyFollows {
	public static enum Variant {
		imd(new DfgMiningParametersIMd()), imfd(new DfgMiningParametersIMfd()), imcd(new DfgMiningParametersIMcd());

		private final DfgMiningParameters miningParameters;

		private Variant(DfgMiningParameters miningParameters) {
			this.miningParameters = miningParameters;
		}

		public DfgMiningParameters getParameters() {
			return miningParameters;
		}
	}

	public static void main(String[] args) throws Exception {
		boolean help = false;
		Variant algorithm = null;
		File file = null;
		if (args.length != 2) {
			help = true;
		} else {
			try {
				algorithm = Variant.valueOf(args[0].toLowerCase());
			} catch (IllegalArgumentException e) {
				help = true;
			}

			file = new File(args[1]);
			help = help || !file.exists();
		}

		if (help) {
			System.out.println("Usage: InductiveMinerDirectlyFollows.jar algorithm dfgfile");
			System.out.println(" Algorithm can be: " + Arrays.toString(Variant.values()) + ".");
			return;
		}

		System.out.println(EfficientTree2HumanReadableString.toMachineString(mineFromDfg(algorithm, file)));
	}

	public static EfficientTree mineFromDfg(Variant algorithm, File file) throws FileNotFoundException, Exception {

		Dfg dfg = new DfgImportPlugin().importFromStream(null, new FileInputStream(file), null, 0);

		EfficientTree tree = ProcessTree2EfficientTree.convert(DfgMiner.mine(dfg, algorithm.miningParameters, new Canceller() {
			public boolean isCancelled() {
				return false;
			}
		}));

		return tree;
	}
   

public static String exportapnToDot(AcceptingPetriNet apn) {

    Petrinet net = apn.getNet();
    Marking initial = apn.getInitialMarking();
    Collection<Marking> finals = apn.getFinalMarkings();

    Set<Place> finalPlaces = new HashSet<>();
    for (Marking fm : finals) {
        for (Object obj : fm) {
            if (obj instanceof Place) finalPlaces.add((Place) obj);
        }
    }

    try (StringWriter stringWriter = new StringWriter();
         BufferedWriter writer = new BufferedWriter(stringWriter)) {

        writer.write("digraph PetriNet {\n");
        writer.write("  rankdir=LR;\n");

        for (Place p : net.getPlaces()) {
            boolean isStart = initial.contains(p);
            boolean isFinal = finalPlaces.contains(p);

            String label = "";
            String extras = "";

            if (isStart) {
                label = "⬤";
            }
            if (isFinal) {
                extras = "peripheries=2";
            }

            writer.write(String.format(
                "  \"%s\" [shape=circle, label=\"%s\", %s];\n",
                p.getId(), label, extras
            ));
        }

        for (Transition t : net.getTransitions()) {
            String label = (t.getLabel() == null || t.getLabel().isEmpty()) ? "τ" : t.getLabel();

            if (label.toLowerCase().contains("tau")) {
                writer.write(String.format(
                    "  \"%s\" [label=\"\", shape=box, style=filled, fillcolor=gray];\n",
                    t.getId()
                ));
            } else {
                writer.write(String.format(
                    "  \"%s\" [label=\"%s\", shape=box, style=filled, fillcolor=lightgray];\n",
                    t.getId(), escape(label)
                ));
            }
        }

        for (Place p : net.getPlaces()) {
            for (Transition t : net.getTransitions()) {
                if (net.getArc(p, t) != null) {
                    writer.write(String.format("  \"%s\" -> \"%s\";\n", p.getId(), t.getId()));
                }
                if (net.getArc(t, p) != null) {
                    writer.write(String.format("  \"%s\" -> \"%s\";\n", t.getId(), p.getId()));
                }
            }
        }

        writer.write("}\n");
        writer.flush();
        return stringWriter.toString();

    } catch (IOException e) {
        e.printStackTrace();
        return "";
    }
}

private static String escape(String s) {
    if (s == null) return "";
    return s.replace("\"", "\\\"");
}


public static String exportTreeToDot(EfficientTree tree) {
    try {
        File tempFile = Files.createTempFile("efficient-tree-", ".txt").toFile();
        EfficientTreeExportPlugin.export(tree, tempFile);

        List<String> lines = Files.readAllLines(tempFile.toPath());
        tempFile.delete();

        StringWriter stringWriter = new StringWriter();
        BufferedWriter writer = new BufferedWriter(stringWriter);

        writer.write("digraph EfficientTree {\n");
        writer.write("  rankdir=TB;\n");

        int nodeId = 0;
        Stack<Integer> parentStack = new Stack<>();
        Stack<Integer> indentStack = new Stack<>();

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            int indent = counter(line);
            String rawLabel = line.trim();
            String label = operatorSymbol(rawLabel);
            String shape;

            if (isOperatorLabel(label)) {
                shape = "circle";
            } else {
                shape = "box";
            }

            while (!indentStack.isEmpty() && indent <= indentStack.peek()) {
                indentStack.pop();
                parentStack.pop();
            }

            writer.write(String.format(
                "  n%d [label=\"%s\", shape=%s, style=filled, fillcolor=white];\n",
                nodeId, escape(label), shape
            ));

            if (!parentStack.isEmpty()) {
                writer.write(String.format("  n%d -> n%d;\n", parentStack.peek(), nodeId));
            }

            parentStack.push(nodeId);
            indentStack.push(indent);
            nodeId++;
        }

        writer.write("}\n");
        writer.flush();
        writer.close();

        return stringWriter.toString();

    } catch (IOException e) {
        e.printStackTrace();
        return "";
    }
}

private static int counter(String line) {
    int count = 0;
    while (count < line.length() && Character.isWhitespace(line.charAt(count))) {
        count++;
    }
    return count;
}

private static String operatorSymbol(String label) {
    switch (label.toLowerCase()) {
        case "sequence": return "→";
        case "xor": return "✕";
        case "concurrent": return "∧";
        case "and": return "∧";
        case "loop": return "⟳";
        case "tau": return "τ";
        default: return label;
    }
}

private static boolean isOperatorLabel(String label) {
    return label.equals("→") ||
           label.equals("∧") ||
           label.equals("✕") ||
           label.equals("⟳");
}

}
