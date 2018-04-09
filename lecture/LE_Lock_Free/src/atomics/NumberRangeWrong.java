package atomics;

import java.util.concurrent.atomic.AtomicReference;

public class NumberRangeWrong {

	private final AtomicReference<NumberRange> range =
			new AtomicReference<>(new NumberRange(0, 0));

	public void setLower(int i) {
		while (true) {
			NumberRange r = range.get();
			if (range.compareAndSet(r, r.setWithLower(i))) {
				return;
			}
		}
	}

	public void setUpper(int i) {
		while (true) {
			NumberRange r = range.get();
			if (range.compareAndSet(r, r.setWithUpper(i))) {
				return;
			}
		}
	}

	public boolean contains(int i) {
		NumberRange r = range.get();
		return r.lower <= i && i <= r.upper;
	}
}

class NumberRange {
	public final int lower;
	public final int upper;

	NumberRange(int lower, int upper) {
		this.lower = lower;
		this.upper = upper;
	}

	public NumberRange setWithLower(int l) {
		if (l > upper) throw new IllegalArgumentException();
		return new NumberRange(l, upper);
	}

	public NumberRange setWithUpper(int u) {
		if (u < lower) throw new IllegalArgumentException();
		return new NumberRange(lower, u);
	}
}
