package minerful.logparser;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskClass;

import org.deckfour.xes.classification.XEventClass;

public class XesTaskClass extends AbstractTaskClass implements TaskClass {
	public XEventClass xEventClass;

	protected XesTaskClass() {
		super();
	}

	public XesTaskClass(XEventClass xEventClass) {
		this.xEventClass = xEventClass;
		super.setName(xEventClass.getId());
	}
	
	@Override
	public int compareTo(TaskClass o) {
		if (o instanceof XesTaskClass) {
			return this.xEventClass.compareTo(((XesTaskClass) o).xEventClass);
		}
		else {
			return super.compareTo(o);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((xEventClass == null) ? 0 : xEventClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj.getClass().equals(getClass()))
			return ((XesTaskClass) obj).xEventClass.equals(xEventClass);
		return super.equals(obj);
	}
}