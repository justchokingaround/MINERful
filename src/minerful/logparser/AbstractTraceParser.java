package minerful.logparser;

public abstract class AbstractTraceParser implements LogTraceParser {

	protected boolean parsing;
	protected ReadingSense readingSense = ReadingSense.ONWARDS;

	@Override
	public boolean isParsing() {
		return parsing;
	}

	@Override
	public ReadingSense reverse() {
		this.readingSense = this.readingSense.switchSenseOfReading();
		return this.readingSense;
	}

	@Override
	public ReadingSense getSenseOfReading() {
		return readingSense;
	}
}