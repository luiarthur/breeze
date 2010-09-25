package scalanlp.graphs
/*
 Copyright 2010 David Hall

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

/**
 * Mixin for specifying WeightedGraphs
 * @author dlwh
 */
trait WeightedGraphs {
  type WeightedGraph[Node,Edge,Weight] = Graph[Node,Edge] with Weighted[Edge,Weight]
  type WeightedDigraph[Node,Edge,Weight] = Digraph[Node,Edge] with Weighted[Edge,Weight]
}

/**
 * Marker trait for weighted graphs.
 */
trait Weighted[Edge,W] { this:(Graph[N,Edge] forSome {type N}) =>
  def weight(e: Edge):W
}

object WeightedGraphs extends WeightedGraphs {
  def fromEdgeList[N,W](edges: (N,N,W)*) = {
    val adjList = edges.groupBy(_._1).mapValues{_.map(e => (e._2,e._3))}
    fromAdjacencyList(adjList);
  }

  def fromAdjacencyList[N,W](adjacencyList: Map[N,Seq[(N,W)]]): WeightedGraph[N,(N,N,W),W] = {
    type Node = N;
    type Edge = (N,N,W);
    new Graph[Node,Edge] with Weighted[Edge,W] {
      def edges = for( (n,adj) <- adjacencyList iterator; m <- adj iterator) yield (n,m._1,m._2);
      override def endpoints(e: Edge):(Node,Node) = (e._1,e._2);
      lazy val nodes = {
        val sinks = for {
          seq <- adjacencyList.valuesIterator;
          (n,w) <- seq.iterator
        } yield n;
        (adjacencyList.keys ++ sinks).toSet
      }
      def successors(n: Node) = {
        adjacencyList.getOrElse(n, Seq.empty).iterator.map(_._1);
      }
      def getEdge(n1: Node, n2: Node) = {
        for(adj <- adjacencyList.get(n1); m <- adj.find(_._1 == n2)) yield (n1,n2,m._2);
      }
      def source(e: Edge): Node = e._1
      def sink(e: Edge): Node = e._2;
      def weight(e: Edge) = e._3

      def edgesFrom(n: Node): Iterator[Edge] = adjacencyList.getOrElse(n,Seq.empty).iterator.map(e => (n,e._1,e._2));

      override def toString() = "Graph[" + adjacencyList + "]";
    }
  }
}
