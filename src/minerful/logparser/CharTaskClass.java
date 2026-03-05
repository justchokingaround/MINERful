package minerful.logparser;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskClass;
import minerful.io.encdec.TaskCharEncoderDecoder;

public class CharTaskClass extends AbstractTaskClass implements TaskClass {
	public final Character charClass;

	public CharTaskClass(Character charClass) {
		this.charClass = charClass;
	}

	@Override
	public int compareTo(TaskClass o) {
		if (o instanceof CharTaskClass)
			return this.charClass.compareTo(((CharTaskClass) o).charClass);
		else
			return super.compareTo(o);
	}

	@Override
	public String getName() {
		return charClass.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((charClass == null) ? 0 : charClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj.getClass().equals(getClass()))
			return ((CharTaskClass) obj).charClass.equals(charClass);
		return super.equals(obj); // This to make CharTaskClass compatible with StringTaskClass or other mono-letter task classes.
	}
}