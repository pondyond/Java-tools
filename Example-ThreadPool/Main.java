package com.testinterface.main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {
	// 创建线程池
	static BlockingQueue queue = new LinkedBlockingQueue();
	static ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 30000, 1,
			TimeUnit.DAYS, queue);

	// 导入文本，"C:\\Users\\shixi_zhaoyang1\\Desktop\\test.txt"并提取请求链接。返回list，该list保存了文件中提取符合要求的所有的链接
	// 返回list中保存形如/2/short_url/info.json?source=209678993&url_short=RAMJJOR字符串
	public static List<String> doInput(String filename, String regex) {
		BufferedReader reader = null;
		List<String> list = new ArrayList<String>();
		try {
			System.out.println("hah");
			File file = new File(filename);
			FileInputStream fileinput = new FileInputStream(file);
			BufferedInputStream fis = new BufferedInputStream(fileinput,
					10 * 1024 * 1024); // 用10M的缓冲读取
			reader = new BufferedReader(new InputStreamReader(fis));
			String tempString = "";
			// 逐行提取字符串
			while ((tempString = reader.readLine()) != null) {
				// 找出符合需要的链接字符串regex如/info.json
				if (tempString.contains(regex)) {
					// 切分字符串，根据“"”,需要转义
					String[] temps = tempString.split("\"");
					// 继续切分字符串，根据“空格”
					String[] noheadtail = temps[1].split(" ");
					list.add(noheadtail[1]);
					// 打印出加入list的数据
					System.out.println(noheadtail[1]);
				}
			}
			// 验证提取数量正确与否
			System.out.println(list.size());
			// 关闭io
			reader.close();

		} catch (Exception e) {

			System.out.println("shit！！！！！！");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	// 为链接加入domain
	// 返回list中保存形如http://i.api.weibo.com/2/short_url/info.json?source=209678993的字符串
	public static List<String> addDomain(String domain, String filename,
			String regex) {

		List<String> oldlist = doInput(filename, regex);
		List<String> newlist = new ArrayList<String>();
		String adddomin;
		Iterator<String> iterator = oldlist.iterator();
		while (iterator.hasNext()) {
			String original = iterator.next();
			adddomin = domain + original;
			newlist.add(adddomin);
			System.out.println(adddomin);
		}
		return newlist;
	}

	// 多线程处理请求
	public static List<String> doByMorethread(final List<String> list) {
		final CountDownLatch countDown = new CountDownLatch(list.size());
		List<Future<String>> futureList = new ArrayList<Future<String>>();
		// 多线程处理get请求，并保存response到List<Future<String>>
		for (int j = 0; j < list.size(); j++) {
			final int index = j;
			// 执行多线程请求，并保存response到List<Future<String>>
			Future<String> thefuture = executor.submit(new Callable<String>() {
				@Override
				public String call() {
					// TODO Auto-generated method stub
					try {
						// 获取url并发送请求
						String url = list.get(index);
						String response = HttpRequest.sendGet(url);
						return response;
					} finally {
						countDown.countDown();
					}

				}
			});
			futureList.add(thefuture);
		}

		return getResponseListAsyn(countDown, futureList, true);
	}

	// 异步转换List<Future<String>>成 List<String>
	public static List<String> getResponseListAsyn(CountDownLatch countDown,
			List<Future<String>> futureList, boolean trimNull) {
		List<String> responseList = new ArrayList<String>();
		long timeout = 3000;
		boolean result = false;
		try {
			result = countDown.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {

		}
		if (result) {
			for (Future<String> future : futureList) {
				try {
					String String = future.get();
					if (trimNull && String == null) {
						continue;
					}
					responseList.add(String);
				} catch (InterruptedException e) {

				} catch (ExecutionException e) {

				}
			}
		} else {
			for (Future<String> future : futureList) {
				try {
					String String = future.get(10, TimeUnit.MILLISECONDS);
					if (trimNull && String == null) {
						continue;
					}
					responseList.add(String);
				} catch (InterruptedException e) {

				} catch (ExecutionException e) {

				} catch (TimeoutException e) {
					future.cancel(true);
				}
			}
		}
		return responseList;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 需要对提取出来的残缺链接加上的域名
		String domain = "http://i.api.weibo.com";
		// 需要提取的文件所在的位置
		String filename = "C:\\Users\\shixi_zhaoyang1\\Desktop\\test.txt";
		// 提取行包含的特征
		String regex = "/info.json";
		// 补全提取出来的残缺链接
		List<String> list = addDomain(domain, filename, regex);
		// 统计已经处理过的数量
		int count = 0;
		// addDomain方法返回的list的长度
		int length = list.size();
		System.out.println(length);
		//
		List<String> response;
		List<String> sub;
		// 防止堆内存溢出，将大的list拆分sub成若干份，每份3000个。批量处理。
		for (int i = 0; i < length; i += 3000) {
			// 最后一份可能不满足3000个，特殊处理
			if (i + 3000> length) {
				sub = list.subList(i, length);
			} else {
				sub = list.subList(i, i + 3000);
			}
			// 获取多线程处理后的结果List<String> response
			response = Main.doByMorethread(sub);
			// 遍历打印出结果
			for (int j = 0; j < response.size(); j++) {
				String s = response.get(j);
				// 打印编号和返回结果
				System.out.println(count);
				System.out.println(s);
				count++;
			}
		}
		// 打印出总共处理的个数
		System.out.println(count);
	}

}
