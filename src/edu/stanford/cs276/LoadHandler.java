package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to 1) load training data from files; 2) build idf from
 * data collections in PA1.
 */
public class LoadHandler {

	public static Map<Query, Map<String, Document>> loadTrainData(
			String feature_file_name) throws Exception {
		File feature_file = new File(feature_file_name);
		if (!feature_file.exists()) {
			System.err.println("Invalid feature file name: " + feature_file_name);
			return null;
		}

		BufferedReader reader = new BufferedReader(new FileReader(feature_file));
		String line = null, url = null, anchor_text = null;
		Query query = null;

		/* Feature dictionary: Query -> (url -> Document) */
		Map<Query, Map<String, Document>> queryDict = new HashMap<Query, Map<String, Document>>();

		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split(":", 2);
			String key = tokens[0].trim();
			String value = tokens[1].trim();

			if (key.equals("query")) {
				query = new Query(value);
				queryDict.put(query, new HashMap<String, Document>());
			} else if (key.equals("url")) {
				url = value;
				queryDict.get(query).put(url, new Document(url));
			} else if (key.equals("title")) {
				queryDict.get(query).get(url).title = new String(value);
			} else if (key.equals("header")) {
				if (queryDict.get(query).get(url).headers == null)
					queryDict.get(query).get(url).headers = new ArrayList<String>();
				queryDict.get(query).get(url).headers.add(value);
			} else if (key.equals("body_hits")) {
				if (queryDict.get(query).get(url).body_hits == null)
					queryDict.get(query).get(url).body_hits = new HashMap<String, List<Integer>>();
				String[] temp = value.split(" ", 2);
				String term = temp[0].trim();
				List<Integer> positions_int;

				if (!queryDict.get(query).get(url).body_hits.containsKey(term)) {
					positions_int = new ArrayList<Integer>();
					queryDict.get(query).get(url).body_hits.put(term, positions_int);
				} else
					positions_int = queryDict.get(query).get(url).body_hits.get(term);

				String[] positions = temp[1].trim().split(" ");
				for (String position : positions)
					positions_int.add(Integer.parseInt(position));

			} else if (key.equals("body_length"))
				queryDict.get(query).get(url).body_length = Integer.parseInt(value);
			else if (key.equals("pagerank"))
				queryDict.get(query).get(url).page_rank = Integer.parseInt(value);
			else if (key.equals("anchor_text")) {
				anchor_text = value;
				if (queryDict.get(query).get(url).anchors == null)
					queryDict.get(query).get(url).anchors = new HashMap<String, Integer>();
			} else if (key.equals("stanford_anchor_count"))
				queryDict.get(query).get(url).anchors.put(anchor_text,
						Integer.parseInt(value));
		}

		reader.close();

		return queryDict;
	}

	// Unserializes from file
	public static Map<String, Double> loadDFs(String idfFile) {
		Map<String, Double> termDocCount = null;
		try {
			FileInputStream fis = new FileInputStream(idfFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			termDocCount = (HashMap<String, Double>) ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException | ClassNotFoundException ioe) {
			ioe.printStackTrace();
			return null;
		}
		return termDocCount;
	}

	// Build document frequencies and then serializes to file
	public static Map<String, Double> buildDFs(String dataDir, String idfFile)
			throws IOException {

		// Get root directory
		String root = dataDir;
		File rootdir = new File(root);
		if (!rootdir.exists() || !rootdir.isDirectory()) {
			System.err.println("Invalid data directory: " + root);
			return null;
		}

		File[] dirlist = rootdir.listFiles();

		int totalDocCount = 0;
		
		// Count number of documents in which each term appears
		Map<String, Double> termDocCount = new HashMap<String, Double>();

		for (File dir : dirlist) {

			File[] filelist = dir.listFiles();

			/* For each file */
			for (File file : filelist) {
				totalDocCount++;

				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					String[] tokens = line.trim().split("\\s+");

					// Create a set of the tokens to avoid counting a token more than one
					// time
					Set<String> uniqueTokens = new HashSet<String>(Arrays.asList(tokens));

					for (String uniqueToken : uniqueTokens) {
						
						if (termDocCount.containsKey(uniqueToken)) {
							// Increment the term doc count
							termDocCount.put(uniqueToken, termDocCount.get(uniqueToken) + 1);
						}
						else {
							termDocCount.put(uniqueToken, 1.0);
						}
					}
				}
				reader.close();
			}
		}

		System.out.println(totalDocCount);

		Map<String, Double> termIdf = new HashMap<String, Double>();

		// Make idf using df
		for (String term : termDocCount.keySet()) {
			// Compute idf for each term with Laplace add-one smoothing
			termIdf.put(term, Math.log((totalDocCount+1) / (termDocCount.get(term)+1)));
		}
		// Add a special keyword for inexistent word's idf value
		// Math.log((totalDocCount+1) / 1)
		termIdf.put("#inexistent#term#", Math.log(totalDocCount+1));

		// Save to file
		try {
			FileOutputStream fos = new FileOutputStream(idfFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(termIdf);
			oos.close();
			fos.close();
		}

		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return termIdf;
	}

}
