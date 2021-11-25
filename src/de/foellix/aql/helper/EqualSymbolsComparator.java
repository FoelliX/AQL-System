package de.foellix.aql.helper;

import java.util.Comparator;

public class EqualSymbolsComparator implements Comparator<Object> {
	private String key;
	private String cutterSymbol;

	public EqualSymbolsComparator(String key) {
		this(key, null);
	}

	public EqualSymbolsComparator(String key, String cutterSymbol) {
		this.key = key.toLowerCase();
		this.cutterSymbol = cutterSymbol;
	}

	@Override
	public int compare(Object o1, Object o2) {
		// Cleanup
		String str1 = o1.toString().toLowerCase();
		String str2 = o2.toString().toLowerCase();
		if (this.cutterSymbol != null) {
			if (str1.contains(this.cutterSymbol)) {
				str1 = str1.substring(0, str1.indexOf(this.cutterSymbol));
			}
			if (str2.contains(this.cutterSymbol)) {
				str2 = str2.substring(0, str2.indexOf(this.cutterSymbol));
			}
		}

		// Compute
		final int d1 = equalSymbolsDistance(this.key, str1);
		final int d2 = equalSymbolsDistance(this.key, str2);
		if (d1 < d2) {
			return 1;
		} else if (d1 > d2) {
			return -1;
		} else {
			return 0;
		}
	}

	public static int equalSymbolsDistance(String key, String string) {
		int value = 0;
		for (int i = 0; i < key.length(); i++) {
			if (i < key.length() && i < string.length() && string.charAt(i) == key.charAt(i)) {
				value += 2;
			} else {
				final String c = String.valueOf(key.charAt(i));
				if (string.contains(c)) {
					value++;
				}
			}
		}
		if (string.length() == key.length()) {
			value += 3;
		} else if (string.length() == key.length() + 1) {
			value += 2;
		} else if (string.length() > key.length()) {
			value++;
		}
		return value;
	}
}