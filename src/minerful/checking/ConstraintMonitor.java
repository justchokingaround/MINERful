package minerful.checking;

import minerful.concept.constraint.Constraint;

public class ConstraintMonitor {
	public enum AnteriorPosteriorJoinOp {
		AND, OR;
	}
	
	public final static String NO_CONSEQUENT = "";
	public final static String ACTIVATION_AT_START = "";
	
	public final String anteriorRegExpTemplate;
	public final String posteriorRegExpTemplate;
	public final String activationRegExpTemplate;
	public final AnteriorPosteriorJoinOp joinOp;
	public final Constraint cns;
	
	public String getAnteriorRegExpTemplate() {
		return anteriorRegExpTemplate;
	}
	public String getPosteriorRegExpTemplate() {
		return posteriorRegExpTemplate;
	}
	public String getActivationRegExpTemplate() {
		return activationRegExpTemplate;
	}
	/**
	 * Contructor for the monitor.
	 * @param cns The constraint to be monitored
	 * @param anteriorRegExpTemplate The reg.exp. template to monitor the target (consequent) on the trace's prefix
	 * @param posteriorRegExpTemplate The reg.exp. template to monitor the target on the trace's suffix (reading the sequence in reverse)
	 * @param activationRegExpTemplate The reg.exp. template to monitor the activation
	 */
	public ConstraintMonitor(Constraint cns, String anteriorRegExpTemplate, String posteriorRegExpTemplate, String activationRegExpTemplate, AnteriorPosteriorJoinOp joinOp) {
		this.cns = cns;
		this.anteriorRegExpTemplate = anteriorRegExpTemplate;
		this.posteriorRegExpTemplate = posteriorRegExpTemplate;
		this.activationRegExpTemplate = activationRegExpTemplate;
		this.joinOp = joinOp;
	}
	/**
	 * Contructor for the monitor.
	 * @param cns The constraint to be monitored
	 * @param consequentRegExpTemplate The reg.exp. template to monitor the target (consequent) on the trace
	 * @param activationRegExpTemplate The reg.exp. template to monitor the activation
	 * @param isConsequentPosterior <code>true</code> if the consequent is for the trace suffix, <code>false</code> otherwise
	 */
	public ConstraintMonitor(Constraint cns, String consequentRegExpTemplate, String activationRegExpTemplate, boolean isConsequentPosterior) {
		this.cns = cns;
		this.joinOp = AnteriorPosteriorJoinOp.AND;
		if (isConsequentPosterior) {
			this.anteriorRegExpTemplate = NO_CONSEQUENT;
			this.posteriorRegExpTemplate = consequentRegExpTemplate;
			this.activationRegExpTemplate = activationRegExpTemplate;
		} else {
			this.anteriorRegExpTemplate = consequentRegExpTemplate;
			this.posteriorRegExpTemplate = NO_CONSEQUENT;
			this.activationRegExpTemplate = activationRegExpTemplate;
		}
	}
	/**
	 * Contructor for the monitor. It assumes the target (consequent) is for the suffix, and 
	 * @param cns The constraint to be monitored
	 * @param anteriorRegExpTemplate The reg.exp. template to monitor the target (consequent) on the trace
	 */
	public ConstraintMonitor(Constraint cns, String anteriorRegExpTemplate) {
		this.cns = cns;
		this.joinOp = AnteriorPosteriorJoinOp.AND;
		this.anteriorRegExpTemplate = anteriorRegExpTemplate;
		this.posteriorRegExpTemplate = NO_CONSEQUENT;
		this.activationRegExpTemplate = ACTIVATION_AT_START;
	}
	public String getAnteriorRegExp() {
		if (anteriorRegExpTemplate == NO_CONSEQUENT)
			return NO_CONSEQUENT;
		return getRegExpFromTemplate(anteriorRegExpTemplate);
	}
	public String getPosteriorRegExp() {
		if (posteriorRegExpTemplate == NO_CONSEQUENT)
			return NO_CONSEQUENT;
		return getRegExpFromTemplate(posteriorRegExpTemplate);
	}
	public String getActivationRegExp() {
		if (activationRegExpTemplate == ACTIVATION_AT_START)
			return ACTIVATION_AT_START;
		return getRegExpFromTemplate(activationRegExpTemplate);
	}
	private String getRegExpFromTemplate(String regExpTemplate) {
		return ((cns.getImplied() == null) ? 
				String.format(regExpTemplate, cns.getBase()) : 
				String.format(regExpTemplate, cns.getBase(), cns.getImplied()));
	}
}