package com.fiona.filedemo.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @Description: copy file from src to dest path
 * @Author: lina.feng
 * @Date: 2020/6/11 10:10
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/movefile")
public class FileCopyController {
    private String path = "D:\\lina\\software\\mongo4.2.6\\mongodb\\bin\\test.zip";
    private String dest = "D:\\lina\\software\\mongo4.2.6\\mongodb\\bin\\test\\test.zip";

    @GetMapping("/commom")
    public void commomDownload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        File file = new File(path);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("commomDownload");
        try (InputStream is = new BufferedInputStream(new FileInputStream(file));
             OutputStream out = new FileOutputStream(dest);) {

            byte[] buf = new byte[1024];
            int readLen = 0;

            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                out.write(buf, 0, readLen);
            }

        } catch (Exception e) {
            log.error("download fail : {}", e.getMessage());
        }
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());

    }

    /**
     * 利用 commonio 库下载文件，依赖Apache Common IO ，官网 https://commons.apache.org/proper/commons-io/
     */
    @GetMapping("/FileUtils")
    public void downloadByApacheCommonIO(HttpServletResponse response) {
        try {
            FileUtils.copyFile(new File(path), new File(dest));
            // FileUtils.copyURLToFile(new URL(path), new File(dest));这是网络资源
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/nio/from")
    public void nioDownloadFrom(HttpServletRequest request, HttpServletResponse response) throws IOException {
        File file = new File(path);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("nio move");
        file.getParentFile().mkdirs();
        try (FileInputStream fin = new FileInputStream(new File(path));
             ReadableByteChannel rbc = Channels.newChannel(fin);

             FileOutputStream fos = new FileOutputStream(dest);
             FileChannel foutc = fos.getChannel();
        ) {
            foutc.transferFrom(rbc, 0, Long.MAX_VALUE);

        } catch (Exception e) {
            log.error("download fail : {}", e.getMessage());
        }
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());

    }

    @GetMapping("/nio/to")
    public void nioDownloadTo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        File file = new File(path);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("nio move");
        file.getParentFile().mkdirs();
        try (FileInputStream fin = new FileInputStream(path);
             FileChannel fc = fin.getChannel();

             FileOutputStream fos = new FileOutputStream(dest);
             WritableByteChannel wbc = Channels.newChannel(fos);

        ) {
            fc.transferTo(0, Long.MAX_VALUE, wbc);

        } catch (Exception e) {
            log.error("download fail : {}", e.getMessage());
        }
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());

    }

    @GetMapping("/nio/to/buff")
    public void nioDownloadToBuff(HttpServletRequest request, HttpServletResponse response) throws IOException {
        File file = new File(path);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("niobuff move");
        file.getParentFile().mkdirs();
        if (file.length() > 1024 * 1024 * 10) {
            log.info("大文件");
            try (FileInputStream fin = new FileInputStream(path);
                 FileChannel fc = fin.getChannel();

                 FileOutputStream fos = new FileOutputStream(dest);
                 FileChannel fw = fos.getChannel();
            ) {

                ByteBuffer buff = ByteBuffer.allocateDirect(1024 * 1024);//HeapByteBuffer
                //判断有没有可读数据
                while (fc.read(buff) != -1) {
                    buff.flip();
                    while (buff.hasRemaining()) {
                        fw.write(buff);
                    }
                    buff.clear();

                }

            } catch (Exception e) {
                log.error("download fail : {}", e.getMessage());
            }
        } else {
            log.info("小文件");
            try (FileInputStream fin = new FileInputStream(path);
                 FileChannel fc = fin.getChannel();

                 FileOutputStream fos = new FileOutputStream(dest);
                 FileChannel fw = fos.getChannel();
            ) {

                ByteBuffer buff = ByteBuffer.allocate(1024);//HeapByteBuffer
                //判断有没有可读数据
                while (fc.read(buff) != -1) {
                    buff.flip();
                    while (buff.hasRemaining()) {
                        fw.write(buff);
                    }
                    buff.clear();

                }

            } catch (Exception e) {
                log.error("download fail : {}", e.getMessage());
            }
        }

        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        
    }


}
