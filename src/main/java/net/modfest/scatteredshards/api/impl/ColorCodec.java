package net.modfest.scatteredshards.api.impl;

import com.mojang.serialization.Codec;

public class ColorCodec {
	
	public static Codec<Integer> CODEC = Codec.STRING.xmap(ColorCodec::parseColor, ColorCodec::valueOf);
	
	public static int parseColor(String str) {
		if (str.startsWith("#")) str = str.substring(1);
		if (str.length() == 3) {
			int r = hexDigit(str.charAt(0)); r = r | (r << 4);
			int g = hexDigit(str.charAt(1)); g = g | (g << 4);
			int b = hexDigit(str.charAt(2)); b = b | (b << 4);
			return (r << 16) | (g << 8) | b;
		} else if (str.length() == 6) {
			int r = hexDigit(str.charAt(0)) << 4 | hexDigit(str.charAt(1));
			int g = hexDigit(str.charAt(2)) << 4 | hexDigit(str.charAt(3));
			int b = hexDigit(str.charAt(4)) << 4 | hexDigit(str.charAt(5));
			return (r << 16) | (g << 8) | b;
		} else {
			return 0xFFFFFF;
		}
	}
	
	public static String valueOf(int col) {
		col = col & 0xFFFFFF;
		int r = (col >> 16) & 0xFF;
		int g = (col >> 8) & 0xFF;
		int b = col & 0xFF;
		
		String rs = Integer.toHexString(r);
		String gs = Integer.toHexString(g);
		String bs = Integer.toHexString(b);
		while (rs.length()<2) rs = "0" + rs;
		while (gs.length()<2) gs = "0" + gs;
		while (bs.length()<2) bs = "0" + bs;
		
		boolean shortR = (rs.charAt(0) == rs.charAt(1));
		boolean shortG = (gs.charAt(0) == gs.charAt(1));
		boolean shortB = (bs.charAt(0) == bs.charAt(1));
		if (shortR && shortG && shortB) {
			return "#" + rs.charAt(0) + gs.charAt(0) + bs.charAt(0);
		} else {
			return "#" + rs + gs + bs;
		}
	}
	
	private static final char[] HEX_DIGITS = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
	private static final int hexDigit(char ch) {
		ch = Character.toLowerCase(ch);
		for(int i=0; i<HEX_DIGITS.length; i++) {
			if (HEX_DIGITS[i] == ch) return i;
		}
		return 0;
	}
}
