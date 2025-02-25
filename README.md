## Getting Started

### 1️⃣ Launch Kafka with Docker
First, start Kafka and Zookeeper using the provided Docker Compose file:

```sh
docker-compose -f zk-single-kafka-single.yml up -d
```

This will spin up a single-node Kafka and Zookeeper instance.

### 2️⃣ Run the Scala Service
Ensure you have **SBT** installed, then start the service:

```sh
sbt run
```

This will compile and execute `Main.scala`, launching the HTTP server.

Alternatively, you can run `Main.scala` with specific flags:

#### Usage:
```sh
sbt run -- [options]
```

Options:
- `--run-producer` → Read from the JSON file and write the records to Kafka.
- `--topic [topic]` → Set the topic name to be written to. Defaults to `test-topic` otherwise.
- If no arguments are passed, only the Consumer is run. This prevents re-writing records to existing partitions.

### 3️⃣ Access the HTTP Endpoint
Once the service is running, you can query Kafka messages using your browser or a tool like `curl`:

```
http://localhost:9000/topic/<topic>/<offset>?count=<N>
```

- **`<topic>`** → The Kafka topic to read from
- **`<offset>`** → The starting offset for consuming messages
- **`count=<N>`** → The number of messages to retrieve

#### Example Request:
```sh
curl http://localhost:9000/topic/my_topic/10?count=5
```

This fetches **5 messages** starting from offset **10** in `my_topic`.

---

### Notes
- Ensure Kafka is running before starting the Scala service.
- The service automatically connects to Kafka on `localhost:9092`.


