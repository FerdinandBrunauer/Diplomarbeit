package digitalsalzburg;



import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HttpReader {
	public static Document readSiteFromUrl(String url) {
		try {
			Document doc = Jsoup.connect(url).get();
			return doc;
		}catch(Exception e){
			return null;
		}
	}
	
	public static String readClassFromHtml(Document html){
	    Elements divs = html.getElementsByClass("textarea");
		return divs.toString();
	}

	public static void main(String[] args) {
		Document content = readSiteFromUrl("http://service.salzburg.gv.at/museumdb/Index?cmd=museumdetail&klasse=museum.AMuseum&angebot=false&museumid=81&id1=46&id2=122&sprachid=3&sprachenid=3&lang=3&sprache=Deutsch");
		System.out.println(readClassFromHtml(content));
	}
}
