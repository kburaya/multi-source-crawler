package net.bbridge.crawler.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestUtil {

    private TestUtil() {
    }

    public static String readResource(String name) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                TestUtil.class.getClassLoader().getResourceAsStream(name)))) {
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }
}
