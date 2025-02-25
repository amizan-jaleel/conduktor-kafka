package conduktor

import com.twitter.finagle.http._
import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}

object Main {
  def main(args: Array[String]): Unit = {
    /**
     * Usage:
     * --run-producer: Read from the JSON file and write the records to Kafka
     * --topic [topic]: Set the topic name to be written to. Defaults to test-topic otherwise
     *
     * If no arguments are passed, only the Consumer is run. This is to prevent re-writing records to existing partitions
     */

    //Producer props
    val props = new java.util.Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    // Could've used Byte serializer to save space but pointless with only 500 records

    val runProducer = args.contains("--run-producer")
    if(runProducer) {
      val topic = args.find(_ == "--topic").map(t => args.indexOf(t) + 1).map(args).getOrElse {
        println("WARN: Running producer with no topic specified, using the default topic: test-topic")
        "test-topic"
      }

      ConduktorKafkaProducer.writeRecods(props, topic)
    }

    // Consumer props
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("auto.offset.reset", "earliest") // Ensures we start from the beginning if no offset is found
    props.put("enable.auto.commit", "false") // Disable automatic offset commit

    // define 3 consumers to read from partitions in parallel
    val firstConsumer = new ConduktorKafkaConsumer(props)
    val secondConsumer = new ConduktorKafkaConsumer(props)
    val thirdConsumer = new ConduktorKafkaConsumer(props)
    val service: Service[Request, Response] = Service.mk { request =>
      request.method match {
        case Method.Get =>
          val uri = request.uri.split("/")
          if(uri.length < 3 || !uri.lift(1).contains("topic")) {
            val badResp = Response(Status.BadRequest)
            Future.value(badResp)
          }
          try {
            val topic = uri.lift(2).getOrElse("test-topic")
            val offset = uri.lift(3).getOrElse("10").split("\\?")(0).toLong
            val count = request.getParam("count", "10").toInt
            println(s"Processing the top $count records starting at offset $offset for topic $topic")
            for {
              firstPartition <- firstConsumer.read(0, offset, topic)
              secondPartition <- secondConsumer.read(1, offset, topic)
              thirdPartition <- thirdConsumer.read(2, offset, topic)
            } yield {
              val sizes = List.fill(3)(count / 3).zipWithIndex.map { case (base, i) =>
                if (i < count % 3) base + 1 else base
              }
              /**
               * The assignment says to:
               * This will return the next N records the consumer returns from the kafka topic
               * topic_name, starting from offset offset for each partition in the topic
               *
               * I assume this means take N records altogether, with equal parts from each partition
               */
              val records = firstPartition.take(sizes(0)) ++ secondPartition.take(sizes(1)) ++
                thirdPartition.take(sizes(2))
              val response = Response()
              response.setContentString(records.mkString("\r\n"))
              response
            }
          }
          catch {
            case _: Throwable =>
              val response = Response(Status.BadRequest)
              Future.value(response)
          }

        case _ =>
          val response = Response(Status.MethodNotAllowed)
          response.contentString = "405 Method Not Allowed"
          Future.value(response)
      }
    }

    val server = Http.server.serve(":9000", service)
    println("Finagle HTTP server started on http://localhost:9000")
    Await.ready(server)
  }
}