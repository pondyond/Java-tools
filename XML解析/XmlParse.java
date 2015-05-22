package sinaUrl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class XmlParse {

	/**
	 * Dom4j读web.xml
	 *
 	 * 
	 */

	public static void main(String[] args) {
		readRoot();
	}

	public static void readRoot() {
		try {
			SAXReader reader = new SAXReader();
			InputStream in = XmlParse.class.getClassLoader()
					.getResourceAsStream("web.xml");
			Document doc = reader.read(in);
			Element root = doc.getRootElement();
			readNode(root, "读取web.xml得到json or xml");
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	public static void readNode(Element root, String prefix) {
		if (root == null)
			return;
			
		List<String> list= new ArrayList<String>();
		System.out.println(prefix);
		//根据root得到 servlet-mapping元素//
		Iterator<Element> i = root.elementIterator("servlet-mapping");
		while (i.hasNext()) {
			Element servlet_mapping = (Element) i.next();
			//根据servlet-mapping得到 url-pattern
			Iterator<Element> j = servlet_mapping.elementIterator("url-pattern");
			while (j.hasNext()) {
				Element url_pattern = (Element) j.next();
				//得到url-pattern中以.json or .xml结尾的数据
				String text = url_pattern.getTextTrim();
				if (text.endsWith(".json") || text.endsWith(".xml")) {
					//System.out.println(text);
					list.add(text);
				}
			}

		}
		Collections.sort(list,new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String [] a=o1.split("/");
				String [] b=o2.split("/");
				// TODO Auto-generated method stub
				String [] a1=a[a.length-1].split("\\.");
				String [] b1=b[b.length-1].split("\\.");
				return a1[0].compareTo(b1[0]);
			}
		});
		
		Iterator<String>	result=list.iterator();
		System.out.println("list是的");
		while(result.hasNext()){
			
			System.out.println(result.next());
		}

	}

}
