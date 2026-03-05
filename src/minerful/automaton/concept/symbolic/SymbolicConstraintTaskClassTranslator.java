package minerful.automaton.concept.symbolic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.relation.Response;
import minerful.utils.MessagePrinter;

public class SymbolicConstraintTaskClassTranslator {
	public static MessagePrinter logger = MessagePrinter.getInstance(SymbolicConstraintTaskClassTranslator.class);
	
	/* This data structure maps TaskClasses to symbolic characters */
	private Map<AbstractTaskClass, Character> translationMap;

	public SymbolicConstraintTaskClassTranslator(Constraint cns) {
		this.translationMap = new HashMap<AbstractTaskClass, Character>(
				cns.getInvolvedTaskChars().size(), 
				(float)1.0);
		Map<TaskChar, TaskChar> symbolicMapping = cns.getSymbolicMap();
		for (Entry<TaskChar, TaskChar> tChPair: symbolicMapping.entrySet()) { // For every pair in the map...
			// ... take the TaskClass (as from the log) from the key (the actual TaskChar), and the char identifier 
			// (as in the symbolic automaton) from the value (the symbolic TaskChar)
			translationMap.put(tChPair.getKey().taskClass, tChPair.getValue().identifier);
		}
	}

	public boolean contains(AbstractTaskClass taskClass) {
		return translationMap.containsKey(taskClass);
	}

	public Character getSymbol(AbstractTaskClass taskClass) {
		return translationMap.get(taskClass);
	}
	
	public boolean isSymbolOf(Character chr, AbstractTaskClass taskClass) {
		return translationMap.get(taskClass).equals(chr);
	}

	public boolean isInSetOfSymbol(Set<Character> chrSet, AbstractTaskClass taskClass) {
		return chrSet.contains(getSymbol(taskClass));
	}

//	public static void main(String[] args) {
//		SymbolicConstraintTaskClassTranslator tra = new SymbolicConstraintTaskClassTranslator(
//				new Response(new TaskChar('A'), new TaskChar('B')));
//		System.out.println(tra.translationMap);
//		tra = new SymbolicConstraintTaskClassTranslator(
//				new Response(new TaskCharSet(new TaskChar('A'), new TaskChar('B')), new TaskCharSet(new TaskChar('B'))));
//		System.out.println(tra.translationMap);
//	}
}
