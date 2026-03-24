package ai.advent

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.embeddings.local.LLMEmbedder
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.executor.ollama.client.OllamaModels
import ai.koog.rag.base.mostRelevantDocuments
import ai.koog.rag.vector.EmbeddingBasedDocumentStorage
import ai.koog.rag.vector.InMemoryVectorStorage

class DocumentSearchToolSet(
    private val ollamaClient: OllamaClient,
) : ToolSet {
    val llmEmbedder = LLMEmbedder(ollamaClient, OllamaModels.Embeddings.NOMIC_EMBED_TEXT)
    val chunkEmbedder = TextChunkEmbedder(llmEmbedder)
    val documentStorage = EmbeddingBasedDocumentStorage(embedder = chunkEmbedder, storage = InMemoryVectorStorage())

    @Tool
    @LLMDescription("Search for relevant documents about a person. Returns information such as the person's name, birthday, and hobbies.")
    suspend fun searchDocuments(
        @LLMDescription("Query to search relevant documents about a person (name, birthday, hobbies, etc.)")
        query: String,
        @LLMDescription("Maximum number of documents")
        count: Int,
    ): String {
        val relevantDocuments = documentStorage.mostRelevantDocuments(query, count = count).toList()

        if (relevantDocuments.isEmpty()) {
            return "No relevant documents found for the query: $query"
        }

        val result = StringBuilder("Found ${relevantDocuments.size} relevant documents:\n\n")
        relevantDocuments.forEachIndexed { index, document ->
            result.append("Document ${index + 1}: ${document.source}\n")
            result.append("Content: ${document.content}\n\n")
        }
        return result.toString()
    }

    suspend fun prepareKnowledgeBase() {
        ollamaClient.getModelOrNull(OllamaModels.Embeddings.NOMIC_EMBED_TEXT.id, pullIfMissing = true)

        val knowledgeBase =
            listOf(
                TextChunk(
                    source = "alice.md",
                    content =
                        "Alice was born on March 14. Her hobbies are hiking, photography, and cooking exotic cuisines. " +
                            "She loves outdoor adventures and is an avid trail runner.",
                ),
                TextChunk(
                    source = "bob.md",
                    content =
                        "Bob's birthday is July 22. He is passionate about chess, reading science fiction novels, " +
                            "and building mechanical keyboards. Bob also enjoys cycling on weekends.",
                ),
                TextChunk(
                    source = "carol.md",
                    content =
                        "Carol celebrates her birthday on November 5. Her hobbies include painting watercolors, " +
                            "playing the guitar, and attending live concerts. She is also into yoga and meditation.",
                ),
                TextChunk(
                    source = "dave.md",
                    content =
                        "Dave was born on January 30. He loves woodworking, craft beer brewing, and watching football. " +
                            "Dave is also a big fan of board games and hosts game nights every month.",
                ),
                TextChunk(
                    source = "eve.md",
                    content =
                        "Eve's birthday is September 18. She enjoys scuba diving, traveling to tropical destinations, " +
                            "and learning new languages. Eve also likes baking sourdough bread and gardening.",
                ),
            )

        for (chunk in knowledgeBase) {
            documentStorage.store(chunk, Unit)
            println("  [+] ${chunk.source}")
            println("      ${chunk.content}")
        }
        println("Knowledge base ready.\n")
    }
}

data class TextChunk(
    val source: String,
    val content: String,
)
