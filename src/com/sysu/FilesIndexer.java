package com.sysu;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

class FilesIndexer {

    private String indexPath;
    private String docsPath;

    FilesIndexer(String indexPath, String docsPath) {
        // check docs path
        final Path docDir = Paths.get(docsPath);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check it.");
            System.exit(1);
        }

        this.indexPath = indexPath;
        this.docsPath = docsPath;
    }

    void indexFiles() {
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + this.indexPath + "'...");

            Directory indexDir = FSDirectory.open(Paths.get(this.indexPath));
            Analyzer analyzer = new SmartChineseAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            // Use TF/IDF for ranking documents
            Similarity similarity = new ClassicSimilarity();
            iwc.setSimilarity(similarity);

            IndexWriter writer = new IndexWriter(indexDir, iwc);
            indexDocs(writer);

            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }

    /**
     * Indexes the given file using the given writer, or if a directory is given,
     * recurse over files and directories found under the given directory
     *
     * @param writer Writer to the index where the given file/dir info will be stored
     * @throws IOException If there is a low-level I/O error
     */
    private void indexDocs(final IndexWriter writer) throws IOException {
        Path path = Paths.get(this.docsPath);
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexDoc(writer, file);
                    } catch (IOException ignore) {
                        // don't index files that can't be read
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDoc(writer, path);
        }
    }

    private void indexDoc(IndexWriter writer, Path file) throws IOException {
        try {
            String fileContent = FileUtils.readFileToString(new File(file.toString()), StandardCharsets.UTF_8);

            // make a new, empty document
            Document doc = new Document();

            // Add the path of the file as a field named "path"
            // StringField is indexed(i.e. searchable), but don't tokenize
            // the field into separate words and don't index term frequency or positional information
            Field pathField = new StringField("path", file.toString(), Field.Store.YES);

            // Add the content of the file to a field named "contents"
            Field contentField = new TextField("contents", fileContent, Field.Store.YES);

            // add fields into doc
            doc.add(pathField);
            doc.add(contentField);

            System.out.println("adding " + file);
            writer.addDocument(doc);
        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }
}
