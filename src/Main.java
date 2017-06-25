import com.sun.deploy.net.HttpResponse;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
import java.util.ArrayList;

import sun.misc.BASE64Decoder;

/**
 *
 * Created by Ericwyn on 17-6-25.
 */
public class Main {
    public static final int MAX_SEND_NUM=10;

    public static void main(String[] args) throws FileNotFoundException,Exception{
        String restult=sendGet("https://raw.githubusercontent.com/gfwlist/gfwlist/master/gfwlist.txt");
        String[] strs=new String(decryptBASE64(restult)).split("\n");
        createHiwifiGfwList(strs);
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

    private static void createSsGfwList(){

    }

    private static void createHiwifiGfwList(String[] lists){
        ArrayList<String> listOutPut=readPrivateList();
        File dir=new File("GfwLists");
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
            BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter("GfwLists/gw-shadowsocks.dnslist"));
            for(int i=0;i<listOutPut.size();i++){
                bufferedWriter.write("server=/"+listOutPut.get(i)+"/127.0.0.1#53535");
                if(i!=listOutPut.size()-1){
                    bufferedWriter.newLine();
                }
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        }catch (IOException e){
            System.out.println("创建文件时候发生了IO错误");
        }
    }

    private static ArrayList<String> readPrivateList(){
        ArrayList<String> list=new ArrayList<>();
        try {
            BufferedReader bufferedReader=new BufferedReader(new FileReader("private.list"));
            String line=null;
            while ((line=bufferedReader.readLine())!=null){
                list.add(line);
            }
            bufferedReader.close();
        }catch (FileNotFoundException e){
            System.out.println("未找到private.list文件");
        }catch (IOException e){
            System.out.println("发生了IO 错误");
        }
        return list;
    }
}
