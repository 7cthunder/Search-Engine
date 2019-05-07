## Search-Engine

A search engine demo by using Java Lucene.

### Usage
#### Index Docs
```java
String indexPath = "./index";
String docsPath = "./docs";

FilesIndexer indexer = new FilesIndexer(indexPath, docsPath);
indexer.indexFiles();
```

#### Search
```java
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
```

### Features
#### Spell Check
![Spell check](./assets/spellcheck.png)
