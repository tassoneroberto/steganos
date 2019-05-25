package com.teoinf.steganos.algorithms.compression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class LZW {
	private static double MAX_TABLE_SIZE = Math.pow(2, 8);

	public static String compress(String text) {
		double table_Size = 255;
		Map<String, Integer> TABLE = new HashMap<String, Integer>();
		for (int i = 0; i < 255; i++)
			TABLE.put("" + (char) i, i);
		String initString = "";
		List<Integer> encoded_values = new ArrayList<Integer>();
		for (char symbol : text.toCharArray()) {
			String Str_Symbol = initString + symbol;
			if (TABLE.containsKey(Str_Symbol))
				initString = Str_Symbol;
			else {
				encoded_values.add(TABLE.get(initString));

				if (table_Size < MAX_TABLE_SIZE)
					TABLE.put(Str_Symbol, (int) table_Size++);
				initString = "" + symbol;
			}
		}

		if (!initString.equals(""))
			encoded_values.add(TABLE.get(initString));
		StringBuilder sb = new StringBuilder();
		Iterator<Integer> listIterator = encoded_values.listIterator();
		while (listIterator.hasNext()) {
			sb.append(listIterator.next());
			if (listIterator.hasNext())
				sb.append(" ");
		}
		return sb.toString();

	}

	public static String decompress(String compressedText) {
		List<Integer> get_compress_values = new ArrayList<Integer>();
		int table_Size = 255;
		StringTokenizer st = new StringTokenizer(compressedText);
		while (st.hasMoreTokens()) {
			get_compress_values.add(Integer.parseInt(st.nextToken()));
		}
		Map<Integer, String> TABLE = new HashMap<Integer, String>();
		for (int i = 0; i < 255; i++)
			TABLE.put(i, "" + (char) i);
		String Encode_values = "" + (char) (int) get_compress_values.remove(0);
		StringBuffer decoded_values = new StringBuffer(Encode_values);
		String get_value_from_table = null;
		for (int check_key : get_compress_values) {
			if (TABLE.containsKey(check_key))
				get_value_from_table = TABLE.get(check_key);
			else if (check_key == table_Size)
				get_value_from_table = Encode_values + Encode_values.charAt(0);
			decoded_values.append(get_value_from_table);
			if (table_Size < MAX_TABLE_SIZE)
				TABLE.put(table_Size++, Encode_values + get_value_from_table.charAt(0));
			Encode_values = get_value_from_table;
		}
		return decoded_values.toString();
	}

}
