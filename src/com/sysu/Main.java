package com.sysu;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws IOException {
	    String indexPath = "./index";

	    // make spell check
        spellCheckIndex("./spell", "./index");

	    FilesSearcher searcher = new FilesSearcher(indexPath);

	    while (true) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String queryStr = null;
            System.out.println("请输入搜索关键词：(q for quit)");
            try {
                queryStr = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            String[] suggestions = spellCheck(queryStr, "./spell");
            if (suggestions != null && suggestions.length != 0) {
                System.out.println("您可能想输入：");
                for (int i = 0; i < suggestions.length; ++i) {
                    System.out.println((i + 1) + ". " + suggestions[i]);
                }

                System.out.println("按下'1'~'5'选择关键词，'0'表示您想继续原词搜索：");
                String opt = null;
                opt = bufferedReader.readLine();
                if (!opt.equals("0")) {
                    queryStr = suggestions[opt.charAt(0) - '1'];
                }
            }

            try {
                if (queryStr.equals("q")) {
                    break;
                }
                searcher.search(queryStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
	    }
    }

    private static void spellCheckIndex(String spellIndexPath, String indexPath) {
        try {
            Directory directory = FSDirectory.open((new File(spellIndexPath)).toPath());

            SpellChecker checker = new SpellChecker(directory);

            IndexReader reader = DirectoryReader.open(FSDirectory.open((new File(indexPath)).toPath()));
            Dictionary dictionary = new LuceneDictionary(reader, "contents");

            IndexWriterConfig config = new IndexWriterConfig(new SmartChineseAnalyzer());
            checker.indexDictionary(dictionary, config, true);

            reader.close();
            checker.close();
            directory.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String[] spellCheck(String queryStr, String spellIndexPath) {
        try {
            Directory directory = FSDirectory.open((new File(spellIndexPath)).toPath());

            SpellChecker checker = new SpellChecker(directory);

            int numSug = 5;
            String[] suggestions = checker.suggestSimilar(queryStr, numSug);

            checker.close();
            directory.close();
            return suggestions;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }
}
