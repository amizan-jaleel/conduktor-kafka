package conduktor

import play.api.libs.json.{Json, Reads, Writes}

case class Person(
  _id: String,
  name: String,
  dob: String,
  address: Address,
  telephone: String,
  pets: List[String],
  score: Double,
  email: String,
  url: String,
  description: String,
  verified: Boolean,
  salary: Int,
)

case class Address(
  street: String,
  town: String,
  postode: String, //misspelled
)

object Person {
  implicit lazy val AddressReadsJsFmt: Reads[Address] = Json.reads[Address]
  implicit lazy val AddressWritesJsFmt: Writes[Address] = Json.writes[Address]
  implicit lazy val PersonReadsJsFmt: Reads[Person] = Json.reads[Person]
  implicit lazy val PersonWritesJsFmt: Writes[Person] = Json.writes[Person]
}
