package cn.wqgallery.proxy_tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

public class Main {
    public static void main(String[] args) throws Exception {
        //如果没有配置环境  就到对应项目的SDK 打开build-tools选择一个版本文件打开 输入打印的命令
        // 例如这是我项目的  D:/Users/Administrator/AppData/Local/Android/sdk/build-tools/28.0.2
        String dex = "D:/Users/Administrator/AppData/Local/Android/sdk/build-tools/28.0.2/";
        /**
         * 1.制作只包含解密代码的dex文件
         */
        File aarFile = new File("proxy_core/build/outputs/aar/proxy_core-debug.aar");
        File aarTemp = new File("proxy_tools/temp");
        Zip.unZip(aarFile, aarTemp);
        File classesJar = new File(aarTemp, "classes.jar");
        File classesDex = new File(aarTemp, "classes.dex");

        //dx --dex --output out.dex in.jar 打印的是cmd执行的 命令
        //如果没有配置环境  就到对应项目的SDK 打开build-tools选择一个版本文件打开 输入打印的命令
        // 例如这是我项目的  D:/Users/Administrator/AppData/Local/Android/sdk/build-tools/28.0.2

        //cmd命令
        //cmd /c dx --dex --output E:\dongnan\Lsn_11_Demo2\proxy_tools\temp\classes.dex E:\dongnan\Lsn_11_Demo2\proxy_tools\temp\classes.jar
        System.out.println("cmd /c dx --dex --output " + classesDex.getAbsolutePath()
                + " " + classesJar.getAbsolutePath());

        //注意D:/Users/Administrator/AppData/Local/Android/sdk/build-tools/28.0.2/dx 是SDK工具的路径
        String text = "cmd /c" + dex + "dx --dex --output " + classesDex.getAbsolutePath() + " " + classesJar.getAbsolutePath();
        Process process = Runtime.getRuntime().exec(text);

        process.waitFor();
        if (process.exitValue() != 0) {
            throw new RuntimeException("dex error");
        }

        /**
         * 2.加密APK中所有的dex文件
         */
        File apkFile = new File("app/build/outputs/apk/debug/app-debug.apk");
        File apkTemp = new File("app/build/outputs/apk/debug/temp");
        Zip.unZip(apkFile, apkTemp);
        //只要dex文件拿出来加密
        File[] dexFiles = apkTemp.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".dex");
            }
        });
        //AES加密了
        AES.init(AES.DEFAULT_PWD);
        for (File dexFile : dexFiles) {
            byte[] bytes = Utils.getBytes(dexFile);
            byte[] encrypt = AES.encrypt(bytes);
            FileOutputStream fos = new FileOutputStream(new File(apkTemp,
                    "secret-" + dexFile.getName()));
            fos.write(encrypt);
            fos.flush();
            fos.close();
            dexFile.delete();
        }

        /**
         * 3.把dex放入apk解压目录，重新压成apk文件
         */
        classesDex.renameTo(new File(apkTemp, "classes.dex"));
        File unSignedApk = new File("app/build/outputs/apk/debug/app-unsigned.apk");
        Zip.zip(apkTemp, unSignedApk);


        /**
         * 4.对齐
         */

        // zipalign -v -p 4 my-app-unsigned.apk my-app-unsigned-aligned.apk
        File alignedApk = new File("app/build/outputs/apk/debug/app-unsigned-aligned.apk");
        //cmd命令
        System.out.println("cmd /c zipalign -v -p 4 " + unSignedApk.getAbsolutePath() + " " + alignedApk.getAbsolutePath());

        process = Runtime.getRuntime().exec("cmd /c " + dex + "zipalign -v -p 4 " + unSignedApk.getAbsolutePath()
                + " " + alignedApk.getAbsolutePath());
        process.waitFor();
//        if(process.exitValue()!=0){
//            throw new RuntimeException("dex error");
//        }

        /**
         * 5.签名
         */
//        apksigner sign --ks my-release-key.jks --out my-app-release.apk my-app-unsigned-aligned.apk
//        apksigner sign  --ks jks文件地址 --ks-key-alias 别名 --ks-pass pass:jsk密码 --key-pass pass:别名密码 --out  out.apk in.apk

        File signedApk = new File("app/build/outputs/apk/debug/app-signed-aligned.apk");
        File jks = new File("proxy_tools/proxy2.jks");

        //cmd命令
        System.out.println("cmd /c apksigner sign --ks " + jks.getAbsolutePath()
                + " --ks-key-alias jett --ks-pass pass:123456 --key-pass pass:123456 --out "
                + signedApk.getAbsolutePath() + " " + alignedApk.getAbsolutePath());


        process = Runtime.getRuntime().exec("cmd /c " + dex + "apksigner sign --ks " + jks.getAbsolutePath()
                + " --ks-key-alias jett --ks-pass pass:123456 --key-pass pass:123456 --out "
                + signedApk.getAbsolutePath() + " " + alignedApk.getAbsolutePath());
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new RuntimeException("dex error");
        }
        System.out.println("执行成功");

    }
}


