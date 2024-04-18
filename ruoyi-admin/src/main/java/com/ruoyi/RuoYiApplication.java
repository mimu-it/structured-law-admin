package com.ruoyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 启动程序
 *
 * grep -r -I -l $'^\xEF\xBB\xBF' ./
 *
 * admin
 * admin123
 *
 * 备份全库
 * docker exec -it mysql8.0.29 /bin/bash
 * mysqldump -uroot -p123456 structured_law > /20240418-all.sql
 * docker cp 72fa3616eb56:/20240418-all.sql /Volumes/HD-FOR-MAC/BIZ_ENV/sales/绿邦/法派/大数据结构化/数据备份
 *
 * DROP DATABASE structured_law;
 * source /root/pro_sql/20240124-all.sql
 *
 * 
 * @author ruoyi
 */
@EnableAsync
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class RuoYiApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(RuoYiApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  法能手法库启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}
