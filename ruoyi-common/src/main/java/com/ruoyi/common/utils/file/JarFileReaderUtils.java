package com.ruoyi.common.utils.file;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author xiao.hu
 * @date 2024-01-15
 * @apiNote
 */
public class JarFileReaderUtils {


    public static String readConfig(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        BufferedReader JarUrlProcReader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        StringBuilder buffer = new StringBuilder();

        String JarUrlProcStr;
        while((JarUrlProcStr = JarUrlProcReader.readLine()) != null) {
            buffer.append(JarUrlProcStr);
        }

        return buffer.toString();
    }
}
