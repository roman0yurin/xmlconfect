package com.mthaler.xmlconfect

import scala.reflect.ClassTag
import scala.xml.{ Node, Elem, Null }
import scala.language.postfixOps
import scala.language.implicitConversions
import scala.collection.{ immutable => imm }

object CollectionFormats {

  /**
   * Supplies the XmlElemFormat for lists.
   */
  implicit def listFormat[T](implicit format: XmlElemFormat[T]) = new XmlElemFormat[List[T]] {

    override protected def readElem(node: TNode, name: String): List[T] = {
      // get the XML node seq
      val nodeSeq = node.node

      def readChildren(n: Node): Seq[T] = {
        val children: Seq[Node] = n.child
        format match {
          case n: NamedXmlElemFormat[_] => children.collect { case elem: Elem if elem.label == n.intrinsicName => format.read(Left(TNode.id(elem))) }
          case _ => children.collect { case elem: Elem if elem.label == name => format.read(Left(TNode.id(elem))) }
        }

      }

      nodeSeq flatMap (readChildren) toList
    }

    override protected def writeElem0(value: List[T], name: String): TNode = {
      // write each element using the provided XMLElemFormat
      val children = value.flatMap(format.write(_).left.get.apply)
      // create a TNode and return the result
      TNode.id(children)
    }
  }

  /**
   * Supplies the XmlElemFormat for arrays.
   */
  implicit def arrayFormat[T: ClassTag](implicit format: XmlElemFormat[T]) = new XmlElemFormat[Array[T]] {

    override protected def readElem(node: TNode, name: String): Array[T] = {
      // get the XML node seq
      val nodeSeq = node.node

      def readChildren(n: Node): Seq[T] = {
        val children: Seq[Node] = n.child
        format match {
          case n: NamedXmlElemFormat[_] => children.collect { case elem: Elem if elem.label == n.intrinsicName => format.read(Left(TNode.id(elem))) }
          case _ => children.collect { case elem: Elem if elem.label == name => format.read(Left(TNode.id(elem))) }
        }

      }

      nodeSeq flatMap (readChildren) toArray
    }

    override protected def writeElem0(value: Array[T], name: String): TNode = {
      // write each element using the provided XMLElemFormat
      val children = value.toSeq.flatMap(format.write(_).left.get.apply)
      // create a TNode and return the result
      TNode.id(children)
    }
  }

  implicit def immIterableFormat[T: XmlElemFormat] = viaSeq[imm.Iterable[T], T](seq => imm.Iterable(seq: _*))
  implicit def immSeqFormat[T: XmlElemFormat] = viaSeq[imm.Seq[T], T](seq => imm.Seq(seq: _*))
  implicit def immIndexedSeqFormat[T: XmlElemFormat] = viaSeq[imm.IndexedSeq[T], T](seq => imm.IndexedSeq(seq: _*))
  implicit def immLinearSeqFormat[T: XmlElemFormat] = viaSeq[imm.LinearSeq[T], T](seq => imm.LinearSeq(seq: _*))
  implicit def immSetFormat[T: XmlElemFormat] = viaSeq[imm.Set[T], T](seq => imm.Set(seq: _*))
  implicit def vectorFormat[T: XmlElemFormat] = viaSeq[Vector[T], T](seq => Vector(seq: _*))

  /**
   * An XmlElemFormat construction helper that creates a XmlElemFormat for an iterable type I from a builder function
   * List => I.
   */
  def viaSeq[I <: Iterable[T], T](f: imm.Seq[T] => I)(implicit format: XmlElemFormat[T]): XmlElemFormat[I] = new XmlElemFormat[I] {

    override protected def readElem(node: TNode, name: String = "") = {
      // get the XML node seq
      val nodeSeq = node.node

      def readChildren(n: Node): Seq[T] = {
        val children: Seq[Node] = n.child
        format match {
          case n: NamedXmlElemFormat[_] => children.collect { case elem: Elem if elem.label == n.intrinsicName => format.read(Left(TNode.id(elem))) }
          case _ => children.collect { case elem: Elem if elem.label == name => format.read(Left(TNode.id(elem))) }
        }

      }

      f(nodeSeq flatMap (readChildren) toVector)
    }

    override protected def writeElem0(iterable: I, name: String = ""): TNode = {
      // write each element using the provided XMLElemFormat
      val children = iterable.toVector.flatMap(format.write(_).left.get.apply)
      // create a TNode and return the result
      TNode.id(children)
    }
  }
}

object WrappedCollectionFormats {

  /**
   * Supplies the XmlElemFormat for lists.
   */
  implicit def listFormat[T](wrapperName: String = "")(implicit format: XmlElemFormat[T]) = wrappedFormat(wrapperName, CollectionFormats.listFormat[T])

  /**
   * Supplies the XmlElemFormat for arrays.
   */
  implicit def arrayFormat[T: ClassTag](wrapperName: String = "")(implicit format: XmlElemFormat[T]) = wrappedFormat(wrapperName, CollectionFormats.arrayFormat)

  implicit def immIterableFormat[T: XmlElemFormat](wrapperName: String = "") = viaSeq[imm.Iterable[T], T](wrapperName, seq => imm.Iterable(seq: _*))
  implicit def immSeqFormat[T: XmlElemFormat](wrapperName: String = "") = viaSeq[imm.Seq[T], T](wrapperName, seq => imm.Seq(seq: _*))
  implicit def immIndexedSeqFormat[T: XmlElemFormat](wrapperName: String = "") = viaSeq[imm.IndexedSeq[T], T](wrapperName, seq => imm.IndexedSeq(seq: _*))
  implicit def immLinearSeqFormat[T: XmlElemFormat](wrapperName: String = "") = viaSeq[imm.LinearSeq[T], T](wrapperName, seq => imm.LinearSeq(seq: _*))
  implicit def immSetFormat[T: XmlElemFormat](wrapperName: String = "") = viaSeq[imm.Set[T], T](wrapperName, seq => imm.Set(seq: _*))
  implicit def vectorFormat[T: XmlElemFormat](wrapperName: String = "") = viaSeq[Vector[T], T](wrapperName, seq => Vector(seq: _*))

  def viaSeq[I <: Iterable[T], T](wrapperName: String, f: imm.Seq[T] => I)(implicit format: XmlElemFormat[T]): XmlElemFormat[I] = wrappedFormat(wrapperName, CollectionFormats.viaSeq(f))

  private def wrappedFormat[T](wrapperName: String, format: XmlElemFormat[T]): XmlElemFormat[T] = new XmlElemFormat[T] with SimpleXmlElemWriter[T] {

    override protected def readElem(tnode: TNode, name: String): T = {
      if (wrapperName.nonEmpty) {
        val children = tnode.node \ wrapperName
        format.read(Left(TNode.id(children)), name)
      } else {
        format.read(Left(TNode.id(tnode.node)), name)
      }
    }

    protected override def writeElem(value: T, name: String = ""): Node = {
      val result = format.write(value, name)
      if (wrapperName.nonEmpty) {
        elem(wrapperName, Null, result.left.get.apply)
      } else {
        elem(name, Null, result.left.get.apply)
      }
    }
  }
}
