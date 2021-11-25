package de.foellix.aql.helper;

import java.util.Arrays;
import java.util.Comparator;

public class LevenshteinComparator implements Comparator<Object> {
	private String key;
	private String cutterSymbol;

	public LevenshteinComparator(String key) {
		this(key, null);
	}

	public LevenshteinComparator(String key, String cutterSymbol) {
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
		final int d1 = levenshteinDistance(this.key, str1);
		final int d2 = levenshteinDistance(this.key, str2);
		if (d1 > d2) {
			return 1;
		} else if (d1 < d2) {
			return -1;
		} else {
			return 0;
		}
	}

	public static int levenshteinDistance(String str1, String str2) {
		final int[][] dp = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++) {
			for (int j = 0; j <= str2.length(); j++) {
				if (i == 0) {
					dp[i][j] = j;
				} else if (j == 0) {
					dp[i][j] = i;
				} else {
					dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(str1.charAt(i - 1), str2.charAt(j - 1)),
							dp[i - 1][j] + 1, dp[i][j - 1] + 1);
				}
			}
		}

		return dp[str1.length()][str2.length()];
	}

	public static int costOfSubstitution(char char1, char char2) {
		return char1 == char2 ? 0 : 1;
	}

	public static int min(int... numbers) {
		return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
	}
}