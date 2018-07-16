import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class crawler {
	static String root = "E://eclipse/workspace/open163_crawler/src/open163/";

	public static void Connect(String address) {
		HttpURLConnection conn = null;
		URL url = null;
		InputStream in = null;
		BufferedReader reader = null;
		StringBuffer stringBuffer = null;
		try {
			url = new URL(address);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setDoInput(true);
			conn.connect();
			in = conn.getInputStream();
			reader = new BufferedReader(new InputStreamReader(in));
			stringBuffer = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuffer.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
			try {
				in.close();
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String http = stringBuffer.toString();
		int typeNameStart = http.indexOf("<h2 class=\"m-tit-zrkx f-hide f-pr\"><span></span>");
		int typeNameEnd = http.indexOf("</h2></div>", typeNameStart);

		// �ر�����ֻ��ȡһ��ģ��
		int crawlerEnd = http.indexOf("<div class=\"m-g-zrkx m-t-bg\">");

		String typeName = http.substring(typeNameStart + 48, typeNameEnd);
		System.out.println(typeName);

		// �����ļ��У�����

		String courseUrl = "";
		int courseUrlStart = http.indexOf("cimg\" href=\"", typeNameEnd);
		while (courseUrlStart > -1 && courseUrlStart < crawlerEnd) {
			int courseUrlEnd = http.indexOf("\">", courseUrlStart);

			int courseNameEnd = http.indexOf("</h5></a>", courseUrlEnd);
			int courseNameStart = http.lastIndexOf("\">", courseNameEnd);
			String courseName = http.substring(courseNameStart + 2, courseNameEnd);

			if (courseUrl != "")
				courseUrl = courseUrl + "," + courseName + ";" + http.substring(courseUrlStart + 12, courseUrlEnd);
			else
				courseUrl = courseUrl + courseName + ";" + http.substring(courseUrlStart + 12, courseUrlEnd);
			courseUrlStart = http.indexOf("cimg\" href=\"", courseUrlEnd + 1);
		}

		String currentcourseUrl[] = courseUrl.split(",");
		for (int i = 0; i < currentcourseUrl.length; i++) {
			System.out.println(currentcourseUrl[i]);
			getAllOfCurrentCourse(currentcourseUrl[i]);
		}

		// ��ȡ��Ӧҳ��
	}

	public static void getAllOfCurrentCourse(String address) {
		HttpURLConnection conn = null;
		URL url = null;
		InputStream in = null;
		BufferedReader reader = null;
		StringBuffer stringBuffer = null;

		String courseInfo[] = address.split(";");
		String courseName = courseInfo[0];
		String courseUrl = courseInfo[1];
		// System.out.println(courseUrl);

		try {
			url = new URL(courseUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setDoInput(true);
			conn.connect();
			in = conn.getInputStream();
			reader = new BufferedReader(new InputStreamReader(in));
			stringBuffer = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuffer.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
			try {
				in.close();
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// �������ļ���
		mkdir_first(courseName);

		String http = stringBuffer.toString();
		int crawlerStart = http.indexOf("<table class=\"m-clist\" id=\"list2\" style=\"display:none\">");
		String courseEpisode_Url = "";
		int episodeIndexStart = http.indexOf("<td class=\"u-ctitle\">", crawlerStart);
		int urlIndexStart = http.indexOf("<a href=\"", episodeIndexStart);

		while (episodeIndexStart > -1 && urlIndexStart > -1) {
			String episode = http.substring(episodeIndexStart + 49, urlIndexStart - 28);
			int urlIndexEnd = http.indexOf("\">", urlIndexStart);
			String currentUrl = http.substring(urlIndexStart + 9, urlIndexEnd);

			// System.out.println(episodeIndexStart + ":" + urlIndexStart + ":"
			// + urlIndexEnd);
			// System.out.println(episode + "," + currentUrl);

			if (courseEpisode_Url == "")
				courseEpisode_Url = episode + ";" + currentUrl;
			else
				courseEpisode_Url = courseEpisode_Url + "," + episode + ";" + currentUrl;

			episodeIndexStart = http.indexOf("<td class=\"u-ctitle\">", urlIndexEnd + 1);
			urlIndexStart = http.indexOf("<a href=\"", episodeIndexStart + 1);
		}

		String currentEpisode_Url[] = courseEpisode_Url.split(",");

		for (int i = 0; i < currentEpisode_Url.length; i++) {
			String devide[] = currentEpisode_Url[i].split(";");
			String episodeNum = devide[0];
			// System.out.println("episodeNum:" + episodeNum);
			mkdir_second(courseName, episodeNum);
			String episodeUrl = devide[1];
			get_m3u8(courseName, episodeNum, episodeUrl);
		}
	}

	public static void get_m3u8(String parentName, String Name, String Url) {
		HttpURLConnection conn = null;
		URL url = null;
		InputStream in = null;
		BufferedReader reader = null;
		StringBuffer stringBuffer = null;

		try {
			url = new URL(Url);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setDoInput(true);
			conn.connect();
			in = conn.getInputStream();
			reader = new BufferedReader(new InputStreamReader(in));
			stringBuffer = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuffer.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
			try {
				in.close();
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String http = stringBuffer.toString();
		int m3u8IndexStart = http.indexOf("appsrc : '");
		int m3u8IndexEnd = http.indexOf(".m3u8");
		String m3u8Url = http.substring(m3u8IndexStart + 10, m3u8IndexEnd + 5);
		System.out.println(m3u8Url);

		// download m3u8
		try {
			download_m3u8(parentName, Name, m3u8Url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("m3u8������ɣ���ʼ����ts");
		get_ts(parentName, Name, m3u8Url);
	}

	public static void download_m3u8(String parentName, String Name, String Url) throws IOException {
		URL url = new URL(Url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		// ���ó�ʱ��Ϊ3��
		conn.setConnectTimeout(3 * 1000);
		// ��ֹ���γ���ץȡ������403����
		conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

		// �õ�������
		InputStream inputStream = conn.getInputStream();
		// ��ȡ�Լ�����
		byte[] getData = readInputStream(inputStream);

		// �ļ�����λ��
		String savePath = root + parentName + "/" + Name;
		File saveDir = new File(savePath);
		if (!saveDir.exists()) {
			saveDir.mkdir();
		}
		String fileName = Name.substring(2, Name.length() - 2) + ".m3u8";
		File file = new File(saveDir + File.separator + fileName);
		if (!file.exists()) {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(getData);
			if (fos != null) {
				fos.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}

			System.out.println("info:" + url + ":" + fileName + " ���سɹ�");
		} else {
			System.out.println("info:" + url + ":" + fileName + "�Ѵ���");
		}
	}

	public static byte[] readInputStream(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}
		bos.close();
		return bos.toByteArray();
	}

	public static void get_ts(String parentName, String Name, String Url) {
		String filePath = root + parentName + "/" + Name + "/" + Name.substring(2, Name.length() - 2) + ".m3u8";
		String tsFilePath = root + parentName + "/" + Name + "/ts";
		// System.out.println(tsFilePath);
		File file = new File(tsFilePath);

		if (file.exists()) {
			if (file.isDirectory()) {
				System.out.println("�ļ��д���");
			} else {
				System.out.println("����ͬ���ļ����ļ��д���ʧ��");
			}
		} else {
			System.out.println("�ļ��в����ڣ������ļ��У�" + Name + "/ts");
			file.mkdir();
		}

		try {
			// read file content from file
			StringBuffer sb = new StringBuffer("");

			FileReader reader = new FileReader(filePath);
			BufferedReader br = new BufferedReader(reader);

			String str = null;

			while ((str = br.readLine()) != null) {
				sb.append(str + "\r\n");

				if (str.indexOf(".ts") > -1) {
					if (str.indexOf("http") > -1) {
						int strIndexStart = str.lastIndexOf("/");
						String _str = str.substring(strIndexStart + 1);
						download_ts(tsFilePath, _str, str);
					} else {
						// System.out.println(str);
						// String tsPath = Url;
						int tsUrlPrefixIndexEnd = Url.lastIndexOf("/");
						String tsUrlPrefix = Url.substring(0, tsUrlPrefixIndexEnd + 1);
						String tsPath = tsUrlPrefix + str;
						// download ts

						download_ts(tsFilePath, str, tsPath);

						System.out.println(tsPath);
					}
				}

			}

			br.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ts�ϲ�
		System.out.println("ts ȫ���������, ��ʼ�ϲ�ts");
		try {
			conbineTs(tsFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void download_ts(String filePath, String fileName, String Url) throws IOException {
		String savePath = filePath;
		File saveDir = new File(savePath);
		if (!saveDir.exists()) {
			saveDir.mkdir();
		}
		File file = new File(saveDir + File.separator + fileName);
		if (!file.exists()) {

			URL url = new URL(Url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// ���ó�ʱ��Ϊ3��
			conn.setConnectTimeout(3 * 1000);
			// ��ֹ���γ���ץȡ������403����
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

			// �õ�������
			InputStream inputStream = conn.getInputStream();
			// ��ȡ�Լ�����
			byte[] getData = readInputStream(inputStream);

			// �ļ�����λ��

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(getData);
			if (fos != null) {
				fos.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}

			System.out.println("info:" + url + ":" + fileName + " ���سɹ�");
		} else
			System.out.println(saveDir + File.separator + fileName + "�Ѵ���");
	}

	public static void conbineTs(String Path) throws IOException {
		int parentPathIndex = Path.lastIndexOf("/");
		String parentPath = Path.substring(0, parentPathIndex);
		int fileNameIndex = parentPath.lastIndexOf("/");
		String fileName = parentPath.substring(fileNameIndex);
		// System.out.println(parentPath + fileName + ".ts");

		File tsFile = new File(parentPath + fileName + ".ts");
		if (!tsFile.exists()) {
			FileOutputStream fos = new FileOutputStream(tsFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);

			File file = new File(Path); // ��ȡ��file����
			File[] fs = file.listFiles(); // ����path�µ��ļ���Ŀ¼������File������
			// System.out.println(fs.length);
			for (int i = 1; i < fs.length; i++) { // ����File[]����
				if (!fs[i].isDirectory()) { // ����Ŀ¼(���ļ�)�����ӡ
					// �ϲ�
					FileInputStream fis = new FileInputStream(fs[i]);
					BufferedInputStream bis = new BufferedInputStream(fis);

					byte[] b = new byte[1024];// ÿ����20�����ɰ���ʵ���ļ���С����
					int len;
					try {
						while ((len = bis.read(b)) != -1)
							bos.write(b, 0, len);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							bis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					System.out.println(fs[i] + "�Ѻϲ�");
					fis.close();
				}
			}
			bos.close();
			fos.close();
			System.out.println("�ϲ����");
		}
		else
			System.out.println("�ϲ��ļ��Ѵ���");
	}

	public static void mkdir_first(String Name) {
		// String root = "E://eclipse/workspace/open163_crawler/src/open163/";
		String path = root + Name;
		File file = new File(path);
		if (Name.indexOf("/") > -1)
			Name = Name.replace("/", "");
		if (Name.indexOf(":") > -1)
			Name = Name.replace(":", "");
		if (Name.indexOf("*") > -1)
			Name = Name.replace("*", "");
		if (Name.indexOf("") > -1)
			Name = Name.replace("", "");
		if (Name.indexOf("<") > -1)
			Name = Name.replace("<", "");
		if (Name.indexOf(">") > -1)
			Name = Name.replace(">", "");

		if (file.exists()) {
			if (file.isDirectory()) {
				System.out.println("�ļ��д���");
			} else {
				System.out.println("����ͬ���ļ����ļ��д���ʧ��");
			}
		} else {
			System.out.println("�ļ��в����ڣ������ļ��У�" + Name);
			file.mkdir();
		}
	}

	public static void mkdir_second(String parentName, String Name) {
		// String root = "E://eclipse/workspace/open163_crawler/src/open163/";
		String path = root + parentName + "/" + Name;
		File file = new File(path);

		if (file.exists()) {
			if (file.isDirectory()) {
				System.out.println("�ļ��д���");
			} else {
				System.out.println("����ͬ���ļ����ļ��д���ʧ��");
			}
		} else {
			System.out.println("�ļ��в����ڣ������ļ��У�" + Name);
			file.mkdir();
		}
	}

	public static void main(String[] args) {
		Connect("http://open.163.com/special/cuvocw/");
		// getAllOfCurrentCourse("�Ͼ���ѧ������˼�뾭�䡷;http://v.163.com/special/cuvocw/dangdainvxingwenxue.html");
		// System.out.println("[�ڶ���]".length());
		// mkdir_first("�������Դ�ѧ���й��ֵ���Ů����ѧר�⡷");
		/*
		 * try { download_m3u8("�������Դ�ѧ���й��ֵ���Ů����ѧר�⡷", "[��1��]",
		 * "http://mov.bn.netease.com/open-movie/nos/mp4/2013/10/29/S9BEIMOVV_sd.m3u8"
		 * ); } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		// get_ts("�������Դ�ѧ���й��ֵ���Ů����ѧר�⡷",
		// "[��1��]","http://mov.bn.netease.com/open-movie/nos/mp4/2013/10/29/S9BEIMOVV_sd.m3u8");

		/*
		 * try { conbineTs(
		 * "E://eclipse/workspace/open163_crawler/src/open163/����ʦ����ѧ���Ա����Ӱ��/[��6��]/ts"
		 * ); } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

	}
}