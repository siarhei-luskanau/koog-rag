package ai.advent

import ai.koog.embeddings.base.Vector
import ai.koog.embeddings.local.LLMEmbedder
import ai.koog.rag.vector.DocumentEmbedder

class TextChunkEmbedder(
    private val base: LLMEmbedder,
) : DocumentEmbedder<TextChunk> {
    override suspend fun embed(document: TextChunk): Vector = base.embed(document.content)

    override suspend fun embed(text: String): Vector = base.embed(text)

    override fun diff(
        embedding1: Vector,
        embedding2: Vector,
    ): Double = base.diff(embedding1, embedding2)
}
