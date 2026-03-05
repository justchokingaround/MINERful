package minerful.automaton.concept.symbolic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.automaton.AutomatonFactory;
import minerful.automaton.concept.AnnotatedAutomaton;
import minerful.automaton.concept.relevance.VacuityAwareAutomaton;
import minerful.automaton.utils.AutomatonUtils;
import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logparser.StringTaskClass;
import minerful.utils.MessagePrinter;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;

/**
 * Creates an automaton encoding characters that are of interest to a constraint with
 * {@link TaskChar#SYMBOLIC_TASKCHARS TaskChar.SYMBOLIC_TASKCHARS} as its TaskChars and
 * {@link TaskCharEncoderDecoder#WILDCARD_CHAR TaskCharEncoderDecoder.WILDCARD_CHAR})
 * as the other character composing its alphabet.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class SymbolicConstraintAnnotatedAutomatonFactory {
	public static MessagePrinter logger = MessagePrinter.getInstance(SymbolicConstraintAnnotatedAutomatonFactory.class);
	
	private static SymbolicConstraintAnnotatedAutomatonFactory instance;
	private TreeMap<String, AnnotatedAutomaton> symbolicConstraintAutomataDepot =
			new TreeMap<String, AnnotatedAutomaton>();
	
	protected SymbolicConstraintAnnotatedAutomatonFactory() {
		symbolicConstraintAutomataDepot = new TreeMap<String, AnnotatedAutomaton>();
	}
	
	public static SymbolicConstraintAnnotatedAutomatonFactory getInstance() {
		if(instance == null) {
			instance = new SymbolicConstraintAnnotatedAutomatonFactory();
		}
		return instance;
	}
	
	/**
	 * Returns a symbolic constraint automaton of cns.
	 * Notice that this factory keeps a collection of such automata, one per template.
	 * If the requested automaton is already in the collection, then that is returned.
	 * Otherwise, a new one is created, stored, and then returned.
	 * See {@link #buildSymbolicConstraintAutomaton(Constraint) buildSymbolicConstraintAutomaton(Constraint)}.
	 * @param cns A constraint.
	 * @return A symbolic automaton
	 */
	public static AnnotatedAutomaton getSymbolicConstraintAnnotatedAutomaton(Constraint cns) {
		instance = getInstance();
		TreeMap<String, AnnotatedAutomaton> depot = instance.getDepot();
		String key = getKey(cns);
		if (depot.containsKey(key)) {
			return depot.get(key);
		} else {
			return instance.buildSymbolicConstraintAnnotatedAutomaton(cns, key);
		}
	}
	
	/**
	 * Creates a symbolic constraint automaton of cns.
	 * For instance, if cns is Response('Receive payment', 'Send invoice'), it returns an automaton for
	 * Response('0', '1'), where '0' and '1' are symbols as in {@link TaskChar#SYMBOLIC_TASKCHARS TaskChar.SYMBOLIC_TASKCHARS},
	 * over an alphabet consisting of '0', '1', and a wildcard character
	 * (see {@link TaskCharEncoderDecoder#WILDCARD_CHAR TaskCharEncoderDecoder.WILDCARD_CHAR})
	 * which is intended to label transitions for any other task that is in the scope of the constraint
	 * (considering the above example, e.g., 'Ship letter' is covered by the wildcard character).
	 * 
	 * @param cns A constraint
	 * @param key A key to represent the constraint's symbolic automaton
	 * @return A symbolic automaton
	 * @see RelationConstraint#getSymbolic() RelationConstraint.getSymbolic()
	 */
	protected AnnotatedAutomaton buildSymbolicConstraintAnnotatedAutomaton(Constraint cns, String key) {
		cns = cns.getSymbolic(); // Take the symbolic version of the cns, say Response(0,1) in place of Response('Receive payment', 'Send invoice')
		Set<Character> charSet = cns.getInvolvedTaskCharIdentifiers(); // Take its symbolic TaskChars' identifiers
		Automaton automaton = AutomatonFactory.fromRegularExpressions(
				Arrays.asList(cns.getRegularExpression()),
				cns.getInvolvedTaskCharIdentifiers(),
				true); // Add a wildcard character, which labels transitions for any other action that is in the scope of the constraint (considering the above example, e.g., 'Ship letter' is covered by the wildcard character)
		AnnotatedAutomaton annotAutomaton = new AnnotatedAutomaton(
				automaton,
				AutomatonUtils.createAlphabetArray(cns.getInvolvedTaskCharIdentifiers(), true));
		this.symbolicConstraintAutomataDepot.put(key, annotAutomaton);
		return annotAutomaton;
	}
	
	protected TreeMap<String, AnnotatedAutomaton> getDepot() {
		return symbolicConstraintAutomataDepot;
	}
	
	
	/**
	 * Returns a string identifier to represent a constraint in the local
	 * {@link #symbolicConstraintAutomataDepot symbolicConstraintAutomataDepot}.
	 * By default, the template name is used, 
	 * unless the constraint exhibits an overlap of actual parameters.
	 * In such a case, a different automaton should be employed that has special transitions
	 * for the overlapping ones.
	 * @param cns A constraint
	 * @return A string identifier to represent cns in the {@link #symbolicConstraintAutomataDepot depot}.
	 * @see #buildSymbolicConstraintAutomaton(Constraint) buildSymbolicConstraintAutomaton(Constraint)
	 */
	protected static String getKey(Constraint cns) {
		String key = cns.getTemplateName();
		return key;
	}

}