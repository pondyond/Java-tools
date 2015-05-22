/**
 * 
 */
package cn.t.utils;

import cn.t.common.Config;
import cn.t.common.ExcepFactor;
import cn.t.common.SinaurlException;

/**
 * @author tangfulin
 * 
 */
public class Base62Util {

	private static final String seed = Config.getProperty("base62.seed");
	private static final long base = 62;

	public static String base62Encode(final long id) {
		final StringBuilder sb = new StringBuilder();
		long num = id;
		int value;
		while (num > 0) {
			value = (int) (num % base);
			num = num / base;
			sb.append(seed.charAt(value));
		}

		return sb.reverse().toString();
	}

	public static long base62Decode(final String str) throws SinaurlException {
		long num = 0;
		int pos;
		char c;
		for (int i = 0; i < str.length(); ++i) {
			c = str.charAt(i);
			pos = seed.indexOf(c);
			if (pos < 0) {
				throw new SinaurlException(ExcepFactor.E_SINAURL_WRONG_PARAM_VALUE);
			}
			num = num * base + pos;
		}
		return num;
	}

	/**
	 * @param args
	 * @throws SinaurlException
	 */
	public static void main(final String[] args) throws SinaurlException {
		System.out.println(base62Decode("sorry")); // 20 + 40195082
		System.out.println(base62Decode("aNvKqn"));
        System.out.println(base62Encode(201135115602l));
        System.out.println(base62Encode(39682435l));
	}

}
