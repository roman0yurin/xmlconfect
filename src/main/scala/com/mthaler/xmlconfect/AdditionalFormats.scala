package com.mthaler.xmlconfect

import scala.reflect.{ ClassTag, classTag }
import scala.xml.{ MetaData, NodeSeq, Null }

object AdditionalFormats {

  /**
   * Creates a special format that ignores a value
   */
  def ignoreFormat[T](value: T) = new XmlFormat[T] {

    override def read(xml: XML, name: String): T = value

    override def write(obj: T, name: String): XML = Right(Null)
  }

  /**
   * Constructs a XmlFormat from its two parts, XmlReader and XmlWriter.
   */
  def xmlFormat[T](reader: XmlReader[T], writer: XmlWriter[T]) = new XmlFormat[T] {

    def write(obj: T, name: String = "") = writer.write(obj, name)

    def read(xml: XML, name: String = "") = reader.read(xml, name)
  }

  /**
   * Constructs a XmlAttrFormat from its two parts, XmlAttrReader and XmlAttrWriter.
   */
  def xmlAttrFormat[T](reader: XmlAttrReader[T], writer: XmlAttrWriter[T]) = new XmlAttrFormat[T] {

    protected override def writeAttr(obj: T, name: String = ""): MetaData = writer.write(obj, name).right.get

    protected def readAttr(metaData: MetaData, name: String = ""): T = reader.read(Right(metaData), name)
  }

  /**
   * Constructs a XmlElemFormat from its two parts, XmlElemReader and XmlElemWriter.
   */
  def xmlElemFormat[T](reader: XmlElemReader[T], writer: XmlElemWriter[T]) = new XmlElemFormat[T] {

    protected override def writeElem0(obj: T, name: String = "") = writer.write(obj, name).left.get

    protected def readElem(tnode: TNode, name: String = ""): T = reader.read(Left(tnode), name)
  }

  def namedFormat[T <: Product: ClassTag](format: XmlElemFormat[T]) = new XmlElemFormat[T] with NamedXmlElemFormat[T] {

    override def intrinsicName: String = {
      import reflect.runtime.{ currentMirror => cm }
      val sym = cm.classSymbol(classTag[T].runtimeClass)
      val name0 = sym.name.decodedName.toString
      val name1 = if (name0.startsWith("$")) name0.substring(1) else name0
      val index = name1.indexOf('$')
      if (index > 0) name1.substring(0, index) else name1
    }

    override protected def readElem(tnode: TNode, n: String): T = format.read(Left(tnode), intrinsicName)

    override protected def writeElem0(obj: T, n: String): TNode = format.write(obj, intrinsicName).left.get
  }

  def namedFormat[T](format: XmlElemFormat[T], name: String) = new XmlElemFormat[T] with NamedXmlElemFormat[T] {

    override def intrinsicName: String = name

    override protected def readElem(tnode: TNode, n: String): T = format.read(Left(tnode), name)

    override protected def writeElem0(obj: T, n: String): TNode = format.write(obj, name).left.get
  }

  def namedFormat[T](format: XmlElemFormat[T], name: String, transform: NodeSeq => NodeSeq) = new XmlElemFormat[T] with NamedXmlElemFormat[T] {

    override def intrinsicName: String = name

    override protected def readElem(tnode: TNode, n: String): T = format.read(Left(TNode(tnode.node, transform)), name)

    override protected def writeElem0(obj: T, n: String): TNode = {

      format.write(obj, name).left.get
    }
  }
}
