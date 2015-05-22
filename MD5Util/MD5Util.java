/**
 * 
 */
package cn.t.utils;

import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * @author tangfulin
 * 
 */
public class MD5Util {

	public static final int hexbase = 16;

	private static ThreadLocal<MessageDigest> MD5 = new ThreadLocal<MessageDigest>() {
		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance("MD5");
			} catch (final Exception e) {
				e.printStackTrace();
				System.exit(-1);
				return null;
			}
		}
	};

	public static String md5Digest(final String data) {
		return md5Digest(data.getBytes(Charset.forName("UTF-8")));
	}

	public static String md5Digest(final byte[] data) {
		final MessageDigest md5 = MD5.get();
		md5.reset();
		md5.update(data);
		final byte[] digest = md5.digest();
		return encodeHex(digest);
	}

	public static String encodeHex(final byte[] bytes) {
		final StringBuilder buf = new StringBuilder(bytes.length + bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			if ((bytes[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString(bytes[i] & 0xff, 16));
		}
		return buf.toString();
	}

	public static long hexdec(final String hex) {
		long num = 0;
		char c;
		int n = 0;
		for (int i = 0; i < hex.length(); ++i) {
			c = hex.charAt(i);
			if (c >= 'a' && c <= 'f') {
				n = c - 'a' + 10;
			} else {
				n = c - '0';
			}
			num = num * hexbase + n;
		}
		return num;
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final String[] urls = new String[] { "1e", "abc", "11ff", "cceeff" };

		for (final String url : urls) {
			System.out.println(url + " : " + md5Digest(url));
		}
	}

}
