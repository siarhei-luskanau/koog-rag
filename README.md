# koog-rag

A minimal **Retrieval-Augmented Generation (RAG)** demo using
JetBrains' [koog](https://github.com/JetBrains/koog) AI agent framework
with [Ollama](https://ollama.com) for fully local inference.

## What it does

An AI agent maintains an in-memory vector knowledge base, embeds documents and queries using a local
embedding model, performs semantic similarity search, and feeds retrieved context to a local LLM to
answer natural-language questions — all without any cloud API calls.

The bundled demo asks a date-aware question about fictional people stored in the knowledge base (
names, birthdays, hobbies) and returns who has an upcoming birthday.

## Architecture

```
Question
   │
   ▼
AIAgent (koog, ReAct loop, up to 20 iterations)
   │  calls tool
   ▼
searchDocuments(query, count)
   │  vector similarity search
   ▼
InMemoryVectorStorage  ◄──  nomic-embed-text (Ollama)
   │  returns top-k chunks
   ▼
LLM context  ──►  qwen3.5:9b (Ollama)  ──►  Answer
```

## Prerequisites

- JDK 17+
- [Ollama](https://ollama.com) running locally on `http://localhost:11434`

The required models are pulled automatically on first run:

```bash
ollama pull qwen3.5:9b
ollama pull nomic-embed-text
```

## Usage

```bash
./gradlew :rag:run
```

The agent fires a single question based on today's date and prints the answer to stdout.

## Build

```bash
# Build
./gradlew :rag:build

# Lint check
./gradlew ktlintCheck

# Auto-format
./gradlew ktlintFormat
```

## Tech stack

| Component         | Technology                   |
|-------------------|------------------------------|
| Language          | Kotlin                       |
| Build             | Gradle (Kotlin DSL)          |
| Agent framework   | koog (`ai.koog:koog-agents`) |
| Local LLM runtime | Ollama                       |
| Chat model        | `qwen3.5:9b`                 |
| Embedding model   | `nomic-embed-text`           |
| HTTP client       | Ktor                         |
| Coroutines        | kotlinx.coroutines           |

## Project structure

```
koog-rag/
├── rag/src/main/kotlin/ai/advent/
│   ├── Main.kt                   # Entry point — wires Ollama, agent, runs demo
│   ├── DocumentSearchToolSet.kt  # Knowledge base + searchDocuments tool
│   └── TextChunkEmbedder.kt      # Adapter: LLMEmbedder → DocumentEmbedder
├── gradle/libs.versions.toml     # Version catalog
└── build.gradle.kts              # Root build with ktlint
```

## License

See [LICENSE](LICENSE).
