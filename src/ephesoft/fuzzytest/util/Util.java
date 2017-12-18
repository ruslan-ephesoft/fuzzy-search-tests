package ephesoft.fuzzytest.util;

public class Util {
	
	public static final int FS_LONGEST_COMMON_SUBSEQUENCE = 0;
	public static final int FS_BK_TREE = 1;
	public static final int FS_SYMSPELL = 2;
	
	public static boolean isBlank(String val) {
		return (val == null || val.trim().length() == 0);
	}
	
	public static int get2DArrayLength(Object[][] arr) {
		int res = 0;
		for(int i = 0; i < arr.length; i++) {
			res += arr[i].length;
		}
		return res;
	}

}
