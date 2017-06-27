import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import sun.misc.BASE64Decoder;

/**
 *
 * Created by Ericwyn on 17-6-25.
 */
public class Main {
    public static void main(String[] args) throws Exception{
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        Date time = calendar.getTime();//执行任务时间为03:30:00

        Timer timer = new Timer();
        //每天定时3:30进行一次构建
        timer.schedule(new TimerTaskTest(), time, 1000L * 60 * 60 * 24);
    }
}

/**
 * 定时器方法，定时执行任务
 * Created by Ericwyn on 17-6-27
 */
class TimerTaskTest extends TimerTask{
    private static final int MAX_SEND_NUM=10;
    private static final String DIR_PATH="GfwLists/";
    private static final String HiwifiGfwList_PATH=DIR_PATH+"gw-shadowsocks.dnslist";
    private static final String SsGfwList_PATH=DIR_PATH+"proxy.pac";
    private static final String privateList_PATH="private.list";
    private static final SimpleDateFormat sdf_log=new SimpleDateFormat("yy-MM-dd HH:mm:ss");

    @Override
    public void run() {
        try {
            backOldListFile();
            String restult=sendGet("https://raw.githubusercontent.com/gfwlist/gfwlist/master/gfwlist.txt");
            String[] strs=new String(decryptBASE64(restult)).split("\n");
            createHiwifiGfwList(strs);
            createSsGfwList(strs);
            System.out.println(sdf_log.format(new Date())+"————完成了一次构建");
        }catch (Exception e){
            System.out.println(sdf_log.format(new Date())+"————"+e.toString());
        }
    }
    private static String sendGet(String url) throws FileNotFoundException {
        String result="";
        BufferedReader in=null;
        for(int i=0;i<=MAX_SEND_NUM;i++){
            if(i==MAX_SEND_NUM){
                System.out.println("请求次数超过30次，停止此次请求");
                break;
            }
            try {
                URL realURL=new URL(url);
                URLConnection connection=realURL.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
                connection.connect();
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line=null;
                while ((line=in.readLine())!=null){
                    result += line+"\n";
                }
            }catch (SocketTimeoutException e){
                e.printStackTrace();
                System.out.println("请求超时，重试");
                continue;
            }catch (FileNotFoundException e){
                throw e;
            }catch (MalformedURLException e){
                System.out.println("构建URL时候发生异常");
                e.printStackTrace();
                //直接休息5秒，然后再爬
                try{
                    Thread.sleep(5000);
                }catch(Exception any){

                }
                continue;
            }catch (IOException ioe){
                System.out.println("发生IO异常");
                ioe.printStackTrace();
                try{
                    Thread.sleep(5000);
                }catch(Exception any){

                }
                continue;
            }
            try {
                if(in!=null){
                    in.close();
                }
            }catch (IOException e){
                System.out.println("关闭IO流时候发生了异常");
                e.printStackTrace();
            }
            break;
        }

        return result;
    }

    private static byte[] decryptBASE64(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }

    private static void createSsGfwList(String[] lists){
        //载入自定义加速列表
        ArrayList<String> listOutPut=readPrivateList();
        File dir=new File(DIR_PATH);
        if(!dir.isDirectory()){
            dir.mkdir();
        }
        for (String str:lists){
            if(!str.equals("") && !str.startsWith("/") && !str.startsWith("@") && !str.startsWith("[") && !str.startsWith("!")){
                String listTemp=str.replace("||","")
                        .replace("|","")
                        .replace("https://","")
                        .replace("http://","");
                if(listTemp.charAt(0)=='.'){
                    listTemp=listTemp.substring(1,listTemp.length());
                }
                if(listTemp.charAt(listTemp.length()-1)=='/'){
                    listTemp=listTemp.substring(0,listTemp.length()-1);
                }
                if(!listOutPut.contains(listTemp)){
                    listOutPut.add(listTemp);
                }
            }
        }

        try {
            BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(SsGfwList_PATH));
            bufferedWriter.write("// Generated by GfwListAutoUpdate on "+new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
            bufferedWriter.newLine();
            bufferedWriter.write("// https://github.com/Ericwyn/GfwListAutoUpdate");
            bufferedWriter.newLine();
            bufferedWriter.newLine();
            bufferedWriter.write("var domains = {");
            bufferedWriter.flush();
            for (String str:listOutPut){
                bufferedWriter.newLine();
                bufferedWriter.write("  \""+str+"\": 1, ");
                bufferedWriter.flush();
            }
            String end="};\n\nvar proxy = \"SOCKS5 127.0.0.1:1080;SOCKS 127.0.0.1:1080;DIRECT;\";\n" +
                    "var direct = 'DIRECT;';\n" +
                    "\n" +
                    "function FindProxyForURL(url, host) {\n" +
                    "    var lastPos = 0;\n" +
                    "    var domain = host;\n" +
                    "    while(lastPos >= 0) {\n" +
                    "        if (domains[domain]) {\n" +
                    "            return proxy;\n" +
                    "        }\n" +
                    "        lastPos = host.indexOf('.', lastPos + 1);\n" +
                    "        domain = host.slice(lastPos + 1);\n" +
                    "    }\n" +
                    "    return direct;\n" +
                    "}";
            bufferedWriter.newLine();
            bufferedWriter.write(end);
            bufferedWriter.flush();

            bufferedWriter.close();
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("发生了IO 错误");
        }
    }

    private static void createHiwifiGfwList(String[] lists){
        ArrayList<String> listOutPut=readPrivateList();
        File dir=new File(DIR_PATH);
        if(!dir.isDirectory()){
            dir.mkdir();
        }
        for (String str:lists){
            if(!str.equals("") && !str.startsWith("/") && !str.startsWith("@") && !str.startsWith("[") && !str.startsWith("!")){
                String listTemp=str.replace("||","")
                        .replace("|","")
                        .replace("https://","")
                        .replace("http://","");
                if(listTemp.charAt(0)=='.'){
                    listTemp=listTemp.substring(1,listTemp.length());
                }
                if(listTemp.charAt(listTemp.length()-1)=='/'){
                    listTemp=listTemp.substring(0,listTemp.length()-1);
                }
                if(!listOutPut.contains(listTemp)){
                    listOutPut.add(listTemp);
                }
            }
        }
        listOutPut.add(0,new SimpleDateFormat("yyMMdd.HH.mm").format(new Date()));

        try {
            BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(HiwifiGfwList_PATH));
            for(int i=0;i<listOutPut.size();i++){
                bufferedWriter.write("server=/"+listOutPut.get(i)+"/127.0.0.1#53535");
                if(i!=listOutPut.size()-1){
                    bufferedWriter.newLine();
                }
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("创建文件时候发生了IO错误");
        }
    }

    private static ArrayList<String> readPrivateList(){
        ArrayList<String> list=new ArrayList<>();
        try {
            BufferedReader bufferedReader=new BufferedReader(new FileReader(privateList_PATH));
            String line=null;
            while ((line=bufferedReader.readLine())!=null){
                list.add(line);
            }
            bufferedReader.close();
        }catch (IOException e){
            System.out.println("没有找到私有加速列表文件");
        }
        return list;
    }

    //备份旧的ss文件
    private static void backOldListFile(){
        if (backupList(SsGfwList_PATH)){
            System.out.println("备份了Ss的加速列表");
        }
        if(backupList(HiwifiGfwList_PATH)){
            System.out.println("备份了proxy.pac");
        }
    }

    private static boolean backupList(String listPath){
        try {
            File file=new File(listPath);
            if(file.isFile()){
                String createDate=new SimpleDateFormat("yyMMdd").format(new Date(file.lastModified()));
                BufferedReader bufferedReader=new BufferedReader(new FileReader(listPath));
                BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(listPath+"_"+createDate));
                String line =null;
                while ((line=bufferedReader.readLine())!=null){
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
                bufferedReader.close();
                bufferedWriter.close();
                return true;
            }
        }catch (IOException e){
            System.out.println("备份文件时候发生了IO错误");
        }
        return false;
    }
}