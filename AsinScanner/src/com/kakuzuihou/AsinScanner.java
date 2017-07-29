package com.kakuzuihou;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AsinScanner {

	private static String amazonJp = "www.amazon.co.jp";
	private static String amazonUs = "www.amazon.com";
	public static void main(String[] args) {
		// java -jar AsinScanner.jar p1 p2
		String fileName = null;
		String webUrl = null;
		try {
			webUrl = args[0];
			fileName = args[1];
			//webUrl = "http://www.amazon.co.jp/s/ref=lp_89090051_nr_n_0?fst=as%3Aoff&rh=n%3A86731051%2Cn%3A%2186732051%2Cn%3A89090051%2Cn%3A2460011051&bbn=89090051&ie=UTF8&qid=1441281783&rnid=89090051";
			//fileName = "notebook.csv";
		} catch (Exception e) {
			System.out.println("パラメータが不正です。");
		}
		if (fileName.isEmpty() || webUrl.isEmpty()) {
			System.out.println("パラメータが不正です。");
			return;
		}
		if (webUrl.indexOf(amazonJp) == -1 && webUrl.indexOf(amazonUs) == -1) {
			System.out.println("AmazonのURLではない可能性があります、確認してください！");
			return;
		}

		Map<String, String> codeMap = new HashMap<String, String>();
		String preNextPage = "";

		File file = new File("./" + fileName);
		if (!CsvUtil.checkBeforeWritefile(file)) {
			try {
				// get asincode
				toScanAsin(webUrl, codeMap, preNextPage);
				CsvUtil.creatCsv(fileName, codeMap);
			} catch (Exception e) {
				System.out.println("出力ファイル作成中、エラーが発生しました。");
				System.out.println(e.toString());
			}
		} else {
			System.out.println("ファイルが存在しますが、上書き宜しいですか？(y/n)");
			Scanner scan = new Scanner(System.in);
			if ("y".equals(scan.next())) {
				try {
					// get asincode
					toScanAsin(webUrl, codeMap, preNextPage);
					CsvUtil.creatCsv(fileName, codeMap);
				} catch (Exception e) {
					System.out.println("出力ファイル作成中、エラーが発生しました。");
					System.out.println(e.toString());
				}
			} else {
				System.out.println("処理が実行しなく、完了しました。");
			}
		}

	}

	public static void toScanAsin(String webUrl, Map<String, String> codeMap,
			String preNextPage) throws Exception {
		Object amPage = null;
		String targetHtml = "";
		String nextUrl = "";
		Pattern pattern1 = Pattern.compile("/dp/([A-Z0-9]+)");
		Pattern pattern2 = Pattern.compile("data-asin=\"([A-Z0-9]+)\"");
		List<String> list1 = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();
		try {
			amPage = new GetHtml(new URL(webUrl), preNextPage);
			targetHtml = ((GetHtml) amPage).getTargetHtml();
			// System.out.println(targetHtml);
			Matcher matcher1 = pattern1.matcher(targetHtml);
			Matcher matcher2 = pattern2.matcher(targetHtml);
			if (matcher2.find()) {
				list2.add(matcher2.group(1));
				while (matcher2.find()) {
					list2.add(matcher2.group(1));
				}
				for (String asincode : list2) {
					// System.out.println("data-asin:" + asincode);
					codeMap.put(asincode, asincode);
				}
			} else if (matcher1.find()) {
				list1.add(matcher1.group(1));
				while (matcher1.find()) {
					list1.add(matcher1.group(1));
				}
				for (String asincode : list1) {
					codeMap.put(asincode, asincode);
				}
			}
			nextUrl = ((GetHtml) amPage).getNextPageLink();
			preNextPage = webUrl;
			if (!"".equals(nextUrl)) {
				nextUrl = getUrlHead(webUrl) + nextUrl;
				// System.out.println(nextUrl);
				toScanAsin(nextUrl, codeMap, preNextPage);
			}
		} catch (MalformedURLException e) {
			throw(e);
		}
	}

	private static String getUrlHead(String webUrl) {
		amazonJp = "www.amazon.co.jp";
		amazonUs = "www.amazon.com";
		String urlHead = "http://";
		if (webUrl.indexOf(amazonJp) != -1) {
			urlHead = urlHead + amazonJp;
		} else if (webUrl.indexOf(amazonUs) != -1) {
			urlHead = urlHead + amazonUs;
		} else {
			urlHead = null;
		}
		return urlHead;
	}

}

class GetHtml {
	private String charset = "utf8";
	private boolean retFlag = false;
	Map<String, String> codeMapPage = new HashMap<String, String>();
	private String nextPageLink = "";
	private Pattern nextUrlPattern = Pattern.compile("href=\"(.+?)\"");

	StringBuilder sb = new StringBuilder();

	public GetHtml(URL url, String preNextUrl) throws Exception {
		try {
			BufferedReader br = getConnectionUntilOk(url);
			String line;
			boolean cutFlag = false;
			int count = 0;
			int count2 = 0;
			while ((line = br.readLine()) != null) {
				count++;
				if (line.indexOf("s-result-count") != -1) {
					cutFlag = true;
				}
				if (line.indexOf("pagnHy") != -1) {
					cutFlag = false;
				}
				if (cutFlag) {
					sb.append(line);
				}
				if (line.indexOf("pagnNextLink") != -1) {
					count2 = count;
				}
				if (count2 > 0 && count - count2 == 2) {
					// System.out.println("nextpage:" + line);
					Matcher matcherUrl = nextUrlPattern.matcher(line);
					if (matcherUrl.find()) {
						nextPageLink = matcherUrl.group(1);
					}
					break;
				}
			}
			retFlag = true;
			// System.out.println(count);
		} catch (MalformedURLException ex) {
			throw(new Exception("URLが不正です。"));
		} catch (UnknownHostException ex) {
			throw(new Exception("サイトが見つかりません。"));
		} catch (IOException ex) {
			throw(new Exception("サイトへ接続中にエラーが発生しました。"));
		}
	}

	public String getNextPageLink() {
		return nextPageLink;
	}

	public Map<String, String> getAsinCodeMap() {
		return codeMapPage;
	}

	public String getTargetHtml() {
		return sb.toString();
	}

	private BufferedReader getConnectionUntilOk(URL url) throws Exception {
		HttpURLConnection uc = null;
		BufferedInputStream bis = null;
		BufferedReader br = null;
		int count = 0;
		uc = (HttpURLConnection) url.openConnection();
		//System.out.println(uc.getResponseCode());
		if (404 == uc.getResponseCode()) {
			throw(new Exception("URL不正可能性があります、確認してくだいさい！"));
		}
		while (200 != uc.getResponseCode()) {
			uc = (HttpURLConnection) url.openConnection();
			count++;
			if (count > 20) {
				break;
			}
		}
		if (count > 20) {
			throw(new Exception("URL不正可能性があります、確認してくだいさい！"));
		}
		bis = new BufferedInputStream(uc.getInputStream());
		br = new BufferedReader(new InputStreamReader(bis, charset));

		return br;
	}
}
