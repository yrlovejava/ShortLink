import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class Test {

    public static void main(String[] args) {
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("test/createShortLinkTest.txt"));) {
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
}
