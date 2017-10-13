package net.bbridge.crawler.facebook;

public class StringUtils {

    public static int findMatchingQuote(String s, int startPos) {
        for (int pos = s.indexOf('\"', startPos + 1); pos != -1; pos = s.indexOf('\"', pos + 1)) {
            if (pos != 0 && s.charAt(pos - 1) != '\\') {
                return pos;
            }
        }
        return -1;
    }

    public static String replaceUnicode(String text) {
        StringBuilder sb = new StringBuilder(text);
        String UNICODE_PREFIX = "\\u";
        int UNICODE_LENGTH = 4;
        while (true) {
            int start = sb.indexOf(UNICODE_PREFIX);
            int end = start + UNICODE_PREFIX.length() + UNICODE_LENGTH;
            if (start == -1) {
                break;
            }
            String codeString =
                    sb.substring(start + UNICODE_PREFIX.length(), end);
            int code = Integer.parseInt(codeString, 16);
            char c = (char) code;
            sb.replace(start, end, String.valueOf(c));
        }
        return sb.toString();
    }

    public static String findFirstObject(String src) {
        int start = src.indexOf('{');
        if (start == -1) {
            return null;
        }
        int balance = 1;
        int end;
        for (end = start + 1; end < src.length(); end++) {
            switch (src.charAt(end)) {
                case '{':
                    balance++;
                    break;
                case '}':
                    balance--;
                    break;
            }
            if (balance == 0) {
                break;
            }
        }
        if (balance != 0) {
            return null;
        }
        return src.substring(start, end + 1);
    }
}
