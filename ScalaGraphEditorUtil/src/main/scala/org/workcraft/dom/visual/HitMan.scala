/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see [http://www.gnu.org/licenses/].
*
*/

package org.workcraft.dom.visual

import org.workcraft.dependencymanager.advanced.core.GlobalCache._
import org.workcraft.util.Maybe.Util._

import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Iterator
import org.workcraft.graphics.Geometry.createRectangle

import org.workcraft.scala.grapheditor.tools.HitTester

object HitMan {
  type Touchable = org.workcraft.graphics.Touchable
  class Flat[N](contents: => List[N], tp: N => Touchable) {
    // we use a special case of hierarchical hit tester with None representing the root and contents representing its children 
    val instance = new Instance[Option[N]]({
      case None => contents.map(x => Some(x))
      case Some(x) => Nil
    }, mn => mn.map(tp(_)))

    def hit(point: Point2D.Double, filter: N => Boolean): Option[N] =
      instance.hitFirstChild(point, None, mn => mn.map(filter).getOrElse(false)).flatMap(n => n)

    def getHitTester: HitTester[N] = getHitTester(List(_ => true))

    def getHitTester(testers: List[N => Boolean]): HitTester[N] =
      new HitTester[N] {

        override def hitTest(point: Point2D.Double): Option[N] = {
          for (tester <- testers) {
            val n = hit(point, tester)
            if (n.isDefined)
              return n
          }
          return None
        }

        override def boxHitTest(boxStart: Point2D.Double, boxEnd: Point2D.Double): List[N] = {
          HitMan.boxHitTest[Option[N]]({ case Some(n) => Some(tp(n)); case None => None }, contents.map(x => Some(x)), boxStart, boxEnd).map(_.get)
        }
      }
  }

  class Instance[N](hierarchy: N => List[N], tp: N => Option[Touchable]) {

    def filterByBB(nodes: List[N], point: Point2D.Double): List[N] =
      nodes.filter(arg => tp.apply(arg) match {
        case Some(touchable) => touchable.boundingBox.logical.contains(point)
        case None => false
      })

    private def getFilteredChildren(point: Point2D.Double, node: N): List[N] = filterByBB(hierarchy(node), point).reverse

    def hitDeepest(point: Point2D.Double, node: N, filter: N => Boolean): Option[N] = {
      for (n <- getFilteredChildren(point, node)) {
        val result = hitDeepest(point, n, filter)
        if (result.isDefined) return result
      }
      if (filter.apply(node))
        hitBranch(point, node)
      else
        None
    }

    private def hitBranch(point: Point2D.Double, node: N): Option[N] = {
      if (isBranchHit(point, node))
        Some(node)
      else
        None
    }

    def isBranchHit(point: Point2D.Double, node: N): Boolean = {
      tp(node) match {
        case Some(t) if t.hitTest(point) => true
        case _ => {
          for (n <- getFilteredChildren(point, node)) {
            if (isBranchHit(point, n))
              return true
          }
          return false
        }
      }
    }

    def hitFirst(point: Point2D.Double, node: N, filter: N => Boolean): Option[N] = {
      if (filter(node)) hitBranch(point, node)
      else hitFirstChild(point, node, filter)
    }

    def hitFirstChild(point: Point2D.Double, node: N, filter: N => Boolean): Option[N] = {
      for (n <- getFilteredChildren(point, node)) {
        val hit = hitFirst(point, n, filter)

        if (hit.isDefined)
          return hit
      }
      return None
    }

    def hitFirst(point: Point2D.Double, node: N): Option[N] =
      hitFirst(point, node, _ => true)
  }

  /*  public static [N extends Node] Maybe[N] hitTestForSelection(Function[? super N, ? extends Maybe[? extends Touchable]] tp, Point2D.Double point, N node, final Class[N] type) {
    Function[N, Iterable[? extends N]] children = new Function[N, Iterable[? extends N]] {
      @Override
      public Iterable[? extends N] apply(N argument) {
        ArrayList[N] result = new ArrayList[N]
        for(Node n : eval(argument.children))
          result.add(type.cast(n))
        return result
      }
    }
    
    Instance[N] hitMan = new Instance[N](children, tp)
    
    Maybe[N] nd = hitMan.hitFirstChild(point, node, new Function[N, Boolean] {
      @Override
      public Boolean apply(N n) {
        return n instanceof MovableNew
      }
    })

    if (Maybe.Util.isJust(nd))
      nd = hitMan.hitFirstChild(point, node, new Function[Node, Boolean] {
        public Boolean apply(Node n) {
          return n instanceof VisualConnection
        }
      })

    return nd
  } */

  /*  public static Maybe[Node] hitTestForConnection(Function[? super Node, ? extends Maybe[? extends Touchable]] tp, Point2D.Double point, Node node) {
    Instance[Node] hitMan = new Instance[Node](Hierarchy.children, tp)
    
    Maybe[Node] nd = hitMan.hitDeepest(point, node, new Function[Node, Boolean] {
      public Boolean apply(Node n) {
        return n instanceof MovableNew && ! (n instanceof Container)
      }
    })

    if (Maybe.Util.isJust(nd))
      nd = hitMan.hitDeepest(point, node, new Function[Node, Boolean] {
        public Boolean apply(Node n) {
          return n instanceof VisualConnection
        }
      })

    return nd
  } */

  /**
   * The method finds all direct children of the given container, which completely fit inside the given rectangle.
   * @param container The container whose children will be examined
   * @param p1     The top-left corner of the rectangle, in the parent coordinates for the container
   * @param p2     The bottom-right corner of the rectangle
   * @return       The collection of nodes fitting completely inside the rectangle
   */
  def boxHitTest[N](t: N => Option[Touchable], nodes: List[N], p1: Point2D.Double, p2: Point2D.Double) = {
    val rect: Rectangle2D.Double = createRectangle(p1, p2)
    nodes.filter(n => t(n) match {
      case Some(touchable) => if (p1.getX <= p2.getX)
        rect.contains(touchable.boundingBox.logical)
      else rect.intersects(touchable.boundingBox.logical)
      case None => false
    })
  }
}
