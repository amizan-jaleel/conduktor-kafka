package conduktor

import com.twitter.util.{Future, FuturePool}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.TopicPartition

import java.time.Duration
import scala.jdk.CollectionConverters.{IterableHasAsScala, SeqHasAsJava}


class ConduktorKafkaConsumer(
  props: java.util.Properties,
) {
  def read(partition: Int, offset: Long, topic: String): Future[List[String]] = {
    val partitionToRead = new TopicPartition(topic, partition)
    consumer.assign(List(partitionToRead).asJava)
    consumer.seek(partitionToRead, offset)

    // return a Future
    FuturePool.unboundedPool {
      val records: ConsumerRecords[String, String] = consumer.poll(Duration.ofMillis(1000))
      records.asScala.map(_.value).toList
    }
  }

  private[this] val consumer = new KafkaConsumer[String, String](props)
}
