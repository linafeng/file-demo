package com.fiona.filedemo.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Description: download file from server to client by outputstream
 * @Author: lina.feng
 * @Date: 2020/6/11 10:10
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping
public class DownloadController {
    private String path = "D:\\lina\\software\\mongo4.2.6\\mongodb\\bin\\mongos.exe";

    @GetMapping("/commomDownload")
    public void commomDownload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        File file = new File(path);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("commomDownload");
        try (InputStream is = new BufferedInputStream(new FileInputStream(file));) {
            ServletOutputStream out = getOutput(response);
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
            FileUtils.copyFile(new File(path), getOutput(response));
            //FileUtils.copyURLToFile(new URL(url), new File(saveDir, fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/nioDownload")
    public void nioDownload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //这用用的是文件IO处理
        FileInputStream fin = new FileInputStream(path);
        //创建文件的操作管道
        FileChannel fc = fin.getChannel();
        ServletOutputStream out = getOutput(response);
        //分配一个1024个大小缓冲区，说白了就是分配一个10个大小的byte数组
        int bufferSize = 1024;
        ByteBuffer buff = ByteBuffer.allocate(1024);//HeapByteBuffer
        int nGet;
        byte[] byteArr = new byte[bufferSize];
        //判断有没有可读数据
        while (fc.read(buff) != -1) {
            buff.flip();
            while (buff.hasRemaining()) {
                nGet = Math.min(buff.remaining(), bufferSize);
                // read bytes from disk
                buff.get(byteArr, 0, nGet);
                // write bytes to output
                out.write(byteArr);
            }
            buff.clear();

        }
        //最后把管道关闭
        fc.close();
        fin.close();

    }

    private ServletOutputStream getOutput(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream ");
        String fileName = path.substring(path.lastIndexOf(File.separator) + 1);
        StringBuilder contentDisposition = new StringBuilder();
        contentDisposition.append("attachment;filename=\"" + fileName + "\"");
        response.setHeader("Content-Disposition", new String(contentDisposition.toString().getBytes("utf-8"), "iso8859_1"));
        ServletOutputStream out = response.getOutputStream();
        return out;
    }
}
