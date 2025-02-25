package conduktor

import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, NewTopic}
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.config.TopicConfig
import play.api.libs.json.{JsArray, Json}

import scala.io.Source
import scala.jdk.CollectionConverters._

object ConduktorKafkaProducer {
  def writeRecods(
    props: java.util.Properties,
    topic: String,
  ): Unit = {
    val numPartitions = 3
    val replicationFactor: Short = 1

    // Create the topic and the partitions
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    val adminClient = AdminClient.create(props)
    try {
      val topicConfig = Map(
        TopicConfig.CLEANUP_POLICY_CONFIG -> "delete", // not using compaction
        TopicConfig.RETENTION_MS_CONFIG -> "3600000", // 1 hour retention
      ).asJava
      val newTopic = new NewTopic(topic, numPartitions, replicationFactor)
      val createResult = adminClient.createTopics(List(newTopic).asJava)
      createResult.all().get()
      println(s"✅ Topic '$topic' created with $numPartitions partitions.")
    } catch {
      case e: Exception => println(s"⚠️ Error creating topic: ${e.getMessage}")
    } finally {
      adminClient.close()
    }

    // Read the records from the JSON file
    val source = Source.fromResource("random-people-data.json")
    val jsonString = try source.mkString finally source.close()
    val json = (Json.parse(jsonString) \ "ctRoot").validate[JsArray].get
    val people = json.value.map(_.as[Person]).toList

    // Write the messages to Kafka
    val producer = new KafkaProducer[String, String](props)
    people.foreach { person =>
      val id = person._id
      val record = new ProducerRecord[String, String](topic, id, Json.toJson(person).toString())
      producer.send(record)
    }
    producer.close()
  }
}