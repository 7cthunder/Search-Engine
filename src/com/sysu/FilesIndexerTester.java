package com.sysu;


public class FilesIndexerTester {

    public static void main(String[] args) {
        String indexPath = "./index";
        String docsPath = "./docs";

        FilesIndexer indexer = new FilesIndexer(indexPath, docsPath);
        indexer.indexFiles();
    }
}
