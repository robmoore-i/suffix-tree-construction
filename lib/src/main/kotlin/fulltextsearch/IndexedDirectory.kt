package fulltextsearch

class IndexedDirectory(private val indexedFiles: List<IndexedFile>) {
    fun queryCaseSensitive(s: String): List<QueryMatch> {
        return indexedFiles.flatMap { indexedFile ->
            indexedFile.query(s)
        }
    }
}
