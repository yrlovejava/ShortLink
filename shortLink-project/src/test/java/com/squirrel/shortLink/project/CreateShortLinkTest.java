package com.squirrel.shortLink.project;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.squirrel.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.squirrel.shortLink.project.service.ShortLinkService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;

@SpringBootTest
public class CreateShortLinkTest {

    @Resource
    private ShortLinkService shortLinkService;

    /**
     * 生成CSDN短链接创建测试文件
     */
    @Test
    public void makeCSDNShortLinkTestTXT(){
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("D:\\project\\ShortLink\\test\\createShortLinkTest_csdn.txt"))) {
            for(int i = 0;i < 10000;i++){
                int dynamicNumber = 105140431 + i;
                String[] data = {
                        "\"blog.csdn.net\"",
                        "\"https://blog.csdn.net/m0_38044871/article/details/" + dynamicNumber + "\"",
                        "\"默认分组\"",
                        "0",
                        "0",
                        "\"2099-12-03 12:00:00\"",
                        "\"测试短链接\""
                };
                bufferedWriter.write(String.join(",",data));
                bufferedWriter.newLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成掘金短链接创建测试文件
     */
    @Test
    public void createJUEJINShortLinkTestTXT(){
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("D:\\project\\ShortLink\\test\\createShortLinkTest_juejin.txt"))) {
            for(int i = 0;i < 10000;i++){
                long dynamicNumber = 7424017120097075210L + i;
                String[] data = {
                        "\"juejin.cn\"",
                        "\"https://juejin.cn/post/7424017120097075210" + dynamicNumber + "\"",
                        "\"默认分组\"",
                        "0",
                        "0",
                        "\"2099-12-03 12:00:00\"",
                        "\"测试短链接\""
                };
                bufferedWriter.write(String.join(",",data));
                bufferedWriter.newLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成牛客短链接创建测试文件
     */
    @Test
    public void createNIUKEShortLinkTestTXT(){
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("D:\\project\\ShortLink\\test\\createShortLinkTest_niuke.txt"))) {
            String dynamicNumber = "4edc0d8750ef4608b26dd3f72dc2bf87";
            for(int i = 0;i < 10000;i++){
                String[] data = {
                        "\"www.nowcoder.com\"",
                        "\"https://www.nowcoder.com/feed/main/detail/" + dynamicNumber + "?sourceSSR=search\"",
                        "\"默认分组\"",
                        "0",
                        "0",
                        "\"2099-12-03 12:00:00\"",
                        "\"测试短链接\""
                };
                bufferedWriter.write(String.join(",",data));
                bufferedWriter.newLine();
                dynamicNumber = RandomUtil.randomString(32);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 往数据库中插入csdn测试短链接
     */
    @Test
    public void insertCSDNShortLinkTest(){
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader("D:\\project\\ShortLink\\test\\createShortLinkTest_csdn.txt"))) {
            for(int i = 0;i < 10000;i++){
                if(i <= 2000){
                    bufferedReader.readLine();
                    continue;
                }
                String line = bufferedReader.readLine();
                if (StrUtil.isNotBlank(line)){
                    String[] split = line.split(",");
                    ShortLinkCreateReqDTO dto = new ShortLinkCreateReqDTO();
                    dto.setDomain(split[0].substring(1,split[0].length()-1));
                    dto.setOriginUrl(split[1].substring(1,split[1].length()-1));
                    dto.setGid("CSDN测试分组");
                    dto.setCreatedType(0);
                    dto.setValidDateType(0);
                    dto.setValidDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(split[5].substring(1,split[5].length()-1)));
                    dto.setDescribe(split[6].substring(1,split[6].length()-1));
                    shortLinkService.createShortLink(dto);
                    Thread.sleep(200);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
