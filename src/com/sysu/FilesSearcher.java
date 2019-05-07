package com.sysu;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.Date;

class FilesSearcher {
    private String indexPath;

    FilesSearcher(String indexPath) {
        this.indexPath = indexPath;
    }

    void search(String queryStr) throws Exception {

        String field = "contents";

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        // Use TF/IDF for ranking pages
        Similarity similarity = new ClassicSimilarity();

        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);

        Analyzer analyzer = new SmartChineseAnalyzer();

        QueryParser parser = new QueryParser(field, analyzer);
        Query query = parser.parse(queryStr);
        System.out.println("Searching for: " + query.toString(field));

        // calculate indexing time
        Date start = new Date();
        TopDocs results = searcher.search(query, 10);
        ScoreDoc[] hits = results.scoreDocs;
        Date end = new Date();
        int numTotalHits = Math.toIntExact(results.totalHits.value);
        System.out.println(numTotalHits + " total matching documents");
        System.out.println(end.getTime() - start.getTime() + " total milliseconds used.");

        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            String path = doc.get("path");
            if (path != null) {
                System.out.println((i + 1) + ". " + path);
                String title = doc.get("title");
                if (title != null) {
                    System.out.println("    Title： " + doc.get("title"));
                }
            } else {
                System.out.println((i + 1) + ". " + "No path for this document");
            }
        }

        reader.close();
    }
}
