package com.kakuzuihou;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class CsvUtil {
	public static void outputCscFile(String fileName, Map<String, String> map) {
        try {
            File file = new File("./" + fileName);
            
            if (!checkBeforeWritefile(file)){
                creatCsv(fileName, map);
            } else {
                System.out.println("ファイルが存在しますが、上書き宜しいですか？(y/n)");
                Scanner scan = new Scanner(System.in);
                if ("y".equals(scan.next())) {
                    creatCsv(fileName, map);
                } else {
                    System.out.println("出力しなく、処理が完了しました。");
                }
            }

        } catch (IOException ex) {
            //例外時処理
            ex.printStackTrace();
        }
    }
    
    public static boolean checkBeforeWritefile(File file){
        if (file.exists()){
          if (file.isFile() && file.canWrite()){
            return true;
          }
        }

        return false;
    }
    
    public static void creatCsv(String fileName, Map<String, String> map) throws IOException {
        //出力先を作成する
        FileWriter fw = new FileWriter("./" + fileName, false); 
        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
        
        // 件数
        pw.print(map.size());
        pw.println();
        
        // ASINコード
        
        for (String asincode : map.values()) {
            pw.print(asincode);
            pw.println();
        }

        //ファイルに書き出す
        pw.close();

        //終了メッセージを画面に出力する
        System.out.println("抽出したASINコードの出力が完了しました。");
    }


}
