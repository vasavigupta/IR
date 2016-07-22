import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


class CSE535Assignment {

	static int termAtATimeOrCount=0;
	static int termAtATimeAndCount=0;
	
	public static void main(String[] args) {
		BufferedReader br = null;
		BufferedReader br1 = null;
		
		Map<String, LinkedList<Node>> mdocID = new HashMap<String, LinkedList<Node>>();
		Map<String, LinkedList<Node>> mFreq = new HashMap<String, LinkedList<Node>>();
		int k = Integer.parseInt(args[2]);
		String sQueryTerms;
		try {

			String sCurrentLine = "";
			br = new BufferedReader(new FileReader(args[0]));
			while ((sCurrentLine = br.readLine()) != null)
			{
				int docID, freq;
				String[] words = sCurrentLine.split("\\\\c|\\\\m");
				String[] docObj = words[2].replaceAll("[\\[\\]]", "").split(",");

				LinkedList<Node> ll = new LinkedList<Node>();
				LinkedList<Node> ll2 = new LinkedList<>();

				for (int i = 0; i < docObj.length; i++)
				{
					String[] data1 = docObj[i].split("/");
					docID = Integer.parseInt(data1[0].trim());
					freq = Integer.parseInt(data1[1].trim());
					ll.add(new Node(docID, freq));
					ll2.add(new Node(docID, freq));
				}

				Collections.sort(ll, new docID_comp());
				mdocID.put(words[0], ll);

				Collections.sort(ll2, new freq_comp());
				mFreq.put(words[0], ll2);
			}
			
			String r_TopK=getTopK(k, mdocID);
			String finalResult="FUNCTION: getTopK\n"+r_TopK +"\n";
			br1 = new BufferedReader(new FileReader(args[3]));
			String r_getPosting="";
			String r_TAATand="";
			String r_TAATor="";
			String r_DAATand="";
			String r_DAATor="";
			
			while ((sQueryTerms = br1.readLine()) != null)
			{
				String[] aQTerms = sQueryTerms.split(" ");
				for (int i = 0; i < aQTerms.length; i++)
				{
					r_getPosting=getPosting(aQTerms[i], mdocID, mFreq);
					finalResult+="FUNCTION: getPostings "+aQTerms[i]+"\n"+r_getPosting +"\n";
				}
				r_TAATand=termAtATimeQueryAnd(sQueryTerms, mFreq);
				finalResult+="FUNCTION: termAtATimeQueryAnd "+sQueryTerms+"\n"+r_TAATand + "\n";
				r_TAATor=termAtATimeQueryOr(sQueryTerms, mFreq);
				finalResult+="FUNCTION: termAtATimeQueryOr "+sQueryTerms+"\n"+r_TAATor + "\n";
				r_DAATand=docAtATimeQueryAnd(sQueryTerms, mdocID);
				finalResult+="FUNCTION: docAtATimeQueryAnd "+sQueryTerms+"\n"+r_DAATand + "\n";
				r_DAATor=docAtATimeQueryOr(sQueryTerms, mdocID);
				finalResult+="FUNCTION: docAtATimeQueryOr "+sQueryTerms+"\n"+r_DAATor + "\n";
			}
			
		PrintWriter output=new PrintWriter(new FileWriter(new File(args[1])));
		output.write(finalResult);
		output.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	static class Node
	{
		int docID;
		int freq;

		public Node(int a, int b)
		{
			docID = a;
			freq = b;
		}

		public int getdocID()
		{
			return docID;
		}

		public int getfreq()
		{
			return freq;
		}
	}

	static class freq_comp implements Comparator<Node> {
		public int compare(Node e1, Node e2) {
			return (new Integer(e2.freq).compareTo(new Integer(e1.freq)));
		}
	}

	static class docID_comp implements Comparator<Node> {
		public int compare(Node e1, Node e2) {
			if (e1.docID > e2.docID) {
				return 1;
			} else if (e1.docID < e2.docID) {
				return -1;
			}
			return 0;
			// return (new Integer(e2.docID).compareTo(new Integer(e1.docID)));
		}
	}

	public static String getTopK(int k, Map<String, LinkedList<Node>> m1) {

		Set<Entry<String, LinkedList<Node>>> set = m1.entrySet();
		List<Entry<String, LinkedList<Node>>> list = new ArrayList<Entry<String, LinkedList<Node>>>(set);
		Collections.sort(list,new Comparator<Map.Entry<String, LinkedList<Node>>>() {
					public int compare(Map.Entry<String, LinkedList<Node>> o1,
							Map.Entry<String, LinkedList<Node>> o2) {
						return (new Integer((o2.getValue().size()))
								.compareTo(new Integer(o1.getValue().size())));
					}
				});
		String topKTerms = "";

		for (int i = 0; i < k; i++) {
			topKTerms += list.get(i).getKey() + ",";

		}
		return topKTerms.substring(0, topKTerms.length() - 1);
		
	}

	public static String getPosting(String queryTerm,Map<String, LinkedList<Node>> m1, Map<String, LinkedList<Node>> m2)
	{
		if (m1.get(queryTerm) != null) {
			String r_docID=getPostingdocID(queryTerm, m1);
			String r_freq=getPostingfreq(queryTerm, m2);
			String result="Ordered by doc IDs: "+r_docID+"\nOrdered by TF: "+r_freq;
			return result;
		}
		else
			return "Term not found";
	}

	public static String getPostingdocID(String queryTerm,Map<String, LinkedList<Node>> m1) {
		String docIDpl = "";
		for (int i = 0; i < m1.get(queryTerm).size(); i++) {
			docIDpl += String.valueOf((m1.get(queryTerm)).get(i).getdocID())+ ",";
		}
		return (docIDpl.substring(0, docIDpl.length() - 1));
	}

	public static String getPostingfreq(String queryTerm,
			Map<String, LinkedList<Node>> m2) {
		String freqpl = "";
		
		for (int i = 0; i < m2.get(queryTerm).size(); i++) {
			
			freqpl += String.valueOf(((Node) ((m2.get(queryTerm)).get(i)))
					.getdocID()) + ",";
		}
		return (freqpl.substring(0, freqpl.length() - 1));
	}

	public static String termAtATimeQueryAnd(String sQueryTerms,Map<String, LinkedList<Node>> mFreq)
	{
		String[] aQTerms = sQueryTerms.split(" ");
		LinkedHashMap<String, LinkedList<Node>> queryMap = new LinkedHashMap<>();
		boolean flag_term_found=true;
		for (int i = 0; i < aQTerms.length; i++)
		{
			LinkedList<Node> temp = mFreq.get(aQTerms[i]);
			if (temp == null) {
				flag_term_found=false;
			} else {
				queryMap.put(aQTerms[i], temp);
			}
		}
		if(!flag_term_found || queryMap.size()==1)
			return "Terms not found";
		
		LinkedList<Entry<String, LinkedList<Node>>> queryList = new LinkedList<>(queryMap.entrySet());
		
		String r_TAATand = taatAND(queryList, false);
		
		Collections.sort(queryList,new Comparator<Map.Entry<String, LinkedList<Node>>>() {
			public int compare(Map.Entry<String, LinkedList<Node>> o1,Map.Entry<String, LinkedList<Node>> o2)
			{
				return (new Integer((o1.getValue().size())).compareTo(new Integer(o2.getValue().size())));
			}
		});
		r_TAATand = taatAND(queryList, true);
		return r_TAATand;
	
	}

	public static String taatAND(LinkedList<Entry<String, LinkedList<Node>>> queryList, boolean optimized)
	{
		Long start=new Date().getTime();
		int countTAATand=0;
		LinkedList<Node> intersectionList = new LinkedList<>(((Entry<String, LinkedList<Node>>) queryList.get(0)).getValue());
		LinkedList<Node> term;
		for (int i = 1; i < queryList.size(); i++)
		{
			term = ((Entry<String, LinkedList<Node>>) queryList.get(i)).getValue();
			LinkedList<Node> temp = new LinkedList<>();
			for(int j = 0; j < term.size(); j++) {
				for (int k = 0; k < intersectionList.size(); k++) {
					countTAATand++;
					if (intersectionList.get(k).docID == term.get(j).docID) {
						temp.add(term.get(j));
						break;
					}
				}
			}
			intersectionList = temp;
		}
		Collections.sort(intersectionList, new Comparator<Node>() {
				@Override
			public int compare(Node n1, Node n2) {
				if (n1.docID > n2.docID) {
					return 1;
				} else if (n1.docID < n2.docID) {
					return -1;
				} else
					return 0;
			}
		});
		Long end=new Date().getTime();
		Long runtime_taatAND= (end-start);
		
		String result_intersection="";
		for (int i = 0; i < intersectionList.size(); i++)
		{
			result_intersection+=intersectionList.get(i).getdocID() + ",";
		}
		String result="";
		
		
		if(!optimized){
			termAtATimeAndCount=countTAATand;
			return "";
		}
		else
		{	
			result=String.valueOf(intersectionList.size())+ " documents are found\n" + String.valueOf(termAtATimeAndCount) + " comparisons are made\n"+runtime_taatAND+" milliseconds are used\n";
			String result_optimized=result+String.valueOf(countTAATand)+" comparisons are made with optimization\n"+"RESULT: "+result_intersection.substring(0,result_intersection.length()-1);
			return result_optimized;
		}
			
	}

	public static String termAtATimeQueryOr(String sQueryTerms,
			Map<String, LinkedList<Node>> mFreq) {
		String[] aQTerms = sQueryTerms.split(" ");
		LinkedHashMap<String, LinkedList<Node>> queryMap = new LinkedHashMap<>();
		for (int i = 0; i < aQTerms.length; i++) {
			LinkedList<Node> temp = mFreq.get(aQTerms[i]);
			if (temp == null) {
				continue;
			} else {
				queryMap.put(aQTerms[i], temp);
			}
		}
		if(queryMap.size()<1)
			return "Terms not found";

		LinkedList<Entry<String, LinkedList<Node>>> queryListOr = new LinkedList<>(queryMap.entrySet());
		String r_TAATor=taatOR(queryListOr,false); 
		
		//For decreasing order of list based on size of the posting
		Collections.sort(queryListOr,
				new Comparator<Map.Entry<String, LinkedList<Node>>>() {
					public int compare(Map.Entry<String, LinkedList<Node>> o1,
							Map.Entry<String, LinkedList<Node>> o2) {
						return (new Integer((o2.getValue().size()))
								.compareTo(new Integer(o1.getValue().size())));
					}
				});
		r_TAATor=taatOR(queryListOr,true);
		return r_TAATor;
	}
	
	public static String taatOR(LinkedList<Entry<String, LinkedList<Node>>> queryListOr,boolean optimized)
	{
		Long start=new Date().getTime();
		LinkedList<Node> unionList = new LinkedList<>(((Entry<String, LinkedList<Node>>) queryListOr.get(0)).getValue());
			
		LinkedList<Node> termpl;
		int countTAATor=0;
		for (int i = 1; i < queryListOr.size(); i++) {
			boolean flag = true;
			termpl = ((Entry<String, LinkedList<Node>>) queryListOr.get(i)).getValue();
			for (int j = 0; j < termpl.size(); j++) {
				for (int k = 0; k < unionList.size(); k++) {
					countTAATor++;
					if (unionList.get(k).docID == termpl.get(j).docID) {
						flag = false;
						break;
					}
					if (unionList.get(k).docID != termpl.get(j).docID) {
						flag = true;
					}
				}
				if (flag) {
					unionList.add(termpl.get(j));
				}
			}
		}
		Collections.sort(unionList, new Comparator<Node>() {

			@Override
			public int compare(Node n1, Node n2) {
				if (n1.docID > n2.docID) {
					return 1;
				} else if (n1.docID < n2.docID) {
					return -1;
				} else
					return 0;
			}
		});
		Long end=new Date().getTime();
		Long runtime_taatOR= (end-start);
		
		String result_union="";
		for (int i = 0; i < unionList.size(); i++)
		{
			result_union+=unionList.get(i).getdocID() + ",";
		}
		String result="";
		
		
		if(!optimized){
			termAtATimeOrCount=countTAATor;
			return "";
		}
		else
		{
			result=String.valueOf(unionList.size())+ " documents are found\n" + String.valueOf(termAtATimeOrCount) + " comparisons are made\n"+runtime_taatOR+" milliseconds are used\n";
			String result_optimized=result+String.valueOf(countTAATor)+" comparisons are made with optimization\n"+"RESULT: "+result_union.substring(0,result_union.length()-1);
			return result_optimized;
		}
			
	}

	public static String docAtATimeQueryAnd(String sQueryTerms,Map<String, LinkedList<Node>> mdocID)
	{
		Long start=new Date().getTime();
		String[] aQTerms = sQueryTerms.split(" ");
		LinkedHashMap<String, LinkedList<Node>> queryMap = new LinkedHashMap<>();
		int countDAATand=0;
		boolean flag_term_found=true;
		for (int i = 0; i < aQTerms.length; i++) {
			LinkedList<Node> temp = mdocID.get(aQTerms[i]);
			if (temp == null) {
				flag_term_found=false;
			} else {
				queryMap.put(aQTerms[i], temp);
			}
		}
		if(!flag_term_found || queryMap.size()==1)
			return "terms not found";
		
		LinkedList<Entry<String, LinkedList<Node>>> queryList = new LinkedList<>(
				queryMap.entrySet());
		Collections.sort(queryList,
				new Comparator<Map.Entry<String, LinkedList<Node>>>() {
					public int compare(Map.Entry<String, LinkedList<Node>> o1,
							Map.Entry<String, LinkedList<Node>> o2) {
						return (new Integer((o1.getValue().size()))
								.compareTo(new Integer(o2.getValue().size())));
					}
				});

		boolean test = true;
		LinkedList<Node> intersectionList = new LinkedList<>();
		int maxID = -1;
		int maxIndex = -1;
		int[] heads = new int[queryList.size()];
		int[] headValues = new int[queryList.size()];

		while (test) {
			for (int i = 0; i < queryList.size(); i++) {
				Node node = queryList.get(i).getValue().get(heads[i]);
				if (node.docID > maxID) {
					maxID = node.docID;
					maxIndex = i;

				}
				headValues[i] = node.docID;
			}
			for (int j = 0; j < queryList.size(); j++) {
				if (j == maxIndex) {
					continue;// Do nothing if the index is of minimum element
				}

				LinkedList<Node> l = new LinkedList<>(queryList.get(j)
						.getValue());

				int k = heads[j];
				while (k < l.size()) {
					countDAATand++;
					if (l.get(k).docID < maxID) {
						heads[j]++;
						headValues[j] = l.get(k).docID;
						break;
					}
					if (l.get(k).docID >= maxID) {
						break;
					}
					k++;
				}

				if (k == l.size()) {
					// reached end of one list.. end of searching
					test = false;
					break;
				}

				if (test == false) {
					break;
				}
			}

			int equalHeadCount = 0;

			for (int p = 0; p < headValues.length; p++) {
				if (maxID != headValues[p])
					break;
				else {
					equalHeadCount++;
				}
			}
			if (equalHeadCount == headValues.length) {
				intersectionList.add(queryList.get(maxIndex).getValue()
						.get(heads[maxIndex]));
				for (int i = 0; i < heads.length; i++) {
					heads[i]++;
				}
			}

			for (int i = 0; i < headValues.length; i++) {
				if (heads[i] >= queryList.get(i).getValue().size()) {
					test = false;
					break;
				}
			}
		}
		Long end=new Date().getTime();
		Long runtime_daatAND= (end-start);
		
		String result_intersection="";
		for (int i = 0; i < intersectionList.size(); i++)
		{
			result_intersection+=intersectionList.get(i).getdocID() + ",";
		}
		String result="";
		
		result=String.valueOf(intersectionList.size())+ " documents are found\n" + String.valueOf(countDAATand) + " comparisons are made\n"+runtime_daatAND+" milliseconds are used\n"+"RESULT: "+result_intersection.substring(0,result_intersection.length()-1);
		return result;
	}

	public static String docAtATimeQueryOr(String sQueryTerms,
			Map<String, LinkedList<Node>> mdocID) {
		Long start = new Date().getTime();
		int countDAATor=0;
		String[] aQTerms = sQueryTerms.split(" ");
		LinkedHashMap<String, LinkedList<Node>> queryMap = new LinkedHashMap<>();

		for (int i = 0; i < aQTerms.length; i++) {
			LinkedList<Node> temp = mdocID.get(aQTerms[i]);
			if (temp == null) {
				continue;
			} else {
				queryMap.put(aQTerms[i], temp);
			}
		}
		if(queryMap.size()<1)
			return "Terms not found";
		
		LinkedList<Entry<String, LinkedList<Node>>> queryList = new LinkedList<>(
				queryMap.entrySet());
		Collections.sort(queryList,
				new Comparator<Map.Entry<String, LinkedList<Node>>>() {
					public int compare(Map.Entry<String, LinkedList<Node>> o1,
							Map.Entry<String, LinkedList<Node>> o2) {
						return (new Integer((o2.getValue().size()))
								.compareTo(new Integer(o1.getValue().size())));
					}
				});

		LinkedList<Integer> unionList = new LinkedList<>();
		int minID = Integer.MAX_VALUE;
		int[] heads = new int[queryList.size()];
		int[] headValues = new int[queryList.size()];
		boolean test = true;
		for (int i = 0; i < heads.length; i++)
			headValues[i] = queryList.get(i).getValue().get(heads[i]).docID;
		
		while (test) {
			for (int i = 0; i < queryList.size(); i++) {
				if (headValues[i] < minID) {
					minID = headValues[i];
					// minIndex=heads[i];
				}
			}
			unionList.add(minID);

			for (int j = 0; j < queryList.size(); j++) {
				countDAATor++;
				if (heads[j] == (queryList.get(j).getValue().size() - 1)
						&& queryList.get(j).getValue().get(heads[j]).docID == minID) {
					headValues[j] = Integer.MAX_VALUE;
					break;
				} else if (heads[j] == (queryList.get(j).getValue().size() - 1)
						&& queryList.get(j).getValue().get(heads[j]).docID > minID)
					;//unionList.add(queryList.get(j).getValue().get(heads[j]).docID);
				if (headValues[j] == minID) {
					if (heads[j] < queryList.get(j).getValue().size()) {
						heads[j]++;
						headValues[j] = queryList.get(j).getValue()
								.get(heads[j]).docID;

					}

				}
			}
			minID = Integer.MAX_VALUE;
			boolean flag = false;
			for (int p = 0; p < headValues.length; p++) {
				if (headValues[p] == Integer.MAX_VALUE && !flag)
					flag = false;
				else
					flag = true;
			}
			if (!flag)
				test = false;
		}
		Long end=new Date().getTime();
		Long runtime_daatOR= (end-start);
		
		String result_union="";
		for (int i = 0; i < unionList.size(); i++)
		{
			result_union+=unionList.get(i) + ",";
		}
		String result="";
		
		result=String.valueOf(unionList.size())+ " documents are found\n" + String.valueOf(countDAATor) + " comparisons are made\n"+runtime_daatOR+" milliseconds are used\n"+"RESULT: "+result_union.substring(0, result_union.length() - 1);
		return result;
	}
}