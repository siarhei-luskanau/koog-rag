package ai.advent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import kotlinx.coroutines.runBlocking
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Clock
import kotlin.time.toJavaInstant

fun main() =
    runBlocking {
        val ollamaClient = OllamaClient("http://localhost:11434")
        val chatModel =
            LLModel(
                provider = LLMProvider.Ollama,
                id = "qwen3.5:9b",
                capabilities =
                    listOf(
                        LLMCapability.Schema.JSON.Basic,
                        LLMCapability.Speculation,
                        LLMCapability.Temperature,
                        LLMCapability.ToolChoice,
                        LLMCapability.Tools,
                        LLMCapability.Vision.Image,
                    ),
                contextLength = 256_000,
            )
        ollamaClient.getModelOrNull(chatModel.id, pullIfMissing = true)

        val documentSearchToolSet = DocumentSearchToolSet(ollamaClient)
        documentSearchToolSet.prepareKnowledgeBase()

        val agent =
            AIAgent(
                promptExecutor = MultiLLMPromptExecutor(ollamaClient),
                agentConfig =
                    AIAgentConfig(
                        prompt =
                            prompt("rag-agent") {
                                system(
                                    "You are a helpful assistant. When asked questions, use the searchDocuments tool " +
                                        "to find relevant information from the knowledge base. " +
                                        "Answer based on the found documents. " +
                                        "If the tool returns no relevant documents, say you don't have that information.",
                                )
                            },
                        model = chatModel,
                        maxAgentIterations = 20,
                    ),
                toolRegistry =
                    ToolRegistry {
                        tools(documentSearchToolSet.asTools())
                    },
            )

        val today =
            Clock.System
                .now()
                .toJavaInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        val date = today.format(DateTimeFormatter.ofPattern("MMMM d"))
        val question = "Today is $date. Who has a next birthday and what he/she likes?"
        println("Question: $question\n")
        val response = agent.run(question)
        println("Answer: $response")
    }
