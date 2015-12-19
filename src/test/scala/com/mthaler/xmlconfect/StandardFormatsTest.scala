package com.mthaler.xmlconfect

import org.scalatest.FunSuite

import scala.xml.{ Attribute, Null, Text }

class StandardFormatsTest extends FunSuite {

  test("intOption") {
    import BasicAttrFormats._
    val f = StandardFormats.optionFormat[Int]
    assertResult(Some(42)) {
      f.read(Right(Attribute("value", Text("42"), Null)), "value")
    }
    assertResult(Right(Attribute("value", Text("42"), Null))) {
      f.write(Some(42), "value")
    }
    assertResult(None) {
      f.read(Right(Null), "value")
    }
    assertResult(Right(Null)) {
      f.write(None, "value")
    }
    intercept[DeserializationException] {
      f.read(Left(<value>42</value>), "value")
    }
  }

  test("stringOption") {
    import BasicAttrFormats._
    val f = StandardFormats.optionFormat[String]
    assertResult(Some("42")) {
      f.read(Right(Attribute("value", Text("42"), Null)), "value")
    }
    assertResult(Right(Attribute("value", Text("42"), Null))) {
      f.write(Some("42"), "value")
    }
    assertResult(Right(Null)) {
      f.write(None, "value")
    }
    intercept[DeserializationException] {
      f.read(Left(<value>42</value>), "value")
    }
  }

  test("tuple1") {
    import BasicAttrFormats._
    val f = StandardFormats.tuple1Format[String]
    assertResult(Tuple1("test")) {
      f.read(Right(Attribute("_1", Text("test"), Null)))
    }
    assertResult(Right(Attribute("_1", Text("test"), Null))) {
      f.write(Tuple1("test"), "value")
    }
    intercept[DeserializationException] {
      f.read(Left(<test/>))
    }
  }

  test("tuple2") {
    import BasicAttrFormats._
    val f = StandardFormats.tuple2Format[String, Int]
    assertResult(("test", 42)) {
      f.read(Right(Attribute("_1", Text("test"), Null).append(Attribute("_2", Text("42"), Null))))
    }
    assertResult(Right(Attribute("_1", Text("test"), Null).append(Attribute("_2", Text("42"), Null)))) {
      f.write(("test", 42), "value")
    }
    intercept[DeserializationException] {
      f.read(Left(<test/>))
    }
  }

  test("tuple3") {
    import BasicAttrFormats._
    val f = StandardFormats.tuple3Format[String, Int, Boolean]
    assertResult(("test", 42, true)) {
      f.read(Right(Attribute("_1", Text("test"), Null).append(Attribute("_2", Text("42"), Null)).append(Attribute("_3", Text("true"), Null))))
    }
    assertResult(Right(Attribute("_1", Text("test"), Null).append(Attribute("_2", Text("42"), Null)).append(Attribute("_3", Text("true"), Null)))) {
      f.write(("test", 42, true), "value")
    }
    intercept[DeserializationException] {
      f.read(Left(<test/>))
    }
  }
}