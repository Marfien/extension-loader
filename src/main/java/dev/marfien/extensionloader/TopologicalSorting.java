package dev.marfien.extensionloader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import dev.marfien.extensionloader.description.ExtensionDescription;
import dev.marfien.extensionloader.exception.CircularDependencyException;
import dev.marfien.extensionloader.exception.DependencyNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

// This class is inspired by Velocity's PluginDependencyUtil
// (https://github.com/PaperMC/Velocity/blob/dev/3.0.0/proxy/src/main/java/com/velocitypowered/proxy/plugin/util/PluginDependencyUtils.java)
//
// It's an implementation of the Topological Sorting Algorithm
// Wikipedia: https://en.wikipedia.org/wiki/Topological_sorting
//
// Abstract:
// We have a dependency graph where every component is a node.
// The dependency relations are edges with directions.
// At first, every node is removed, that is not needed by another node.
// Now, every node can recalculate its edges, and it can be repeated.
@SuppressWarnings("UnstableApiUsage")
public class TopologicalSorting {

  private static final Logger LOGGER = LoggerFactory.getLogger(TopologicalSorting.class);

  public static List<DiscoveredExtension> sort(final @NotNull List<DiscoveredExtension> extensions) {
    if (extensions.isEmpty()) return List.of();
    final var idMapper = Maps.uniqueIndex(extensions, e -> e.getDescription().id());
    final var descriptions = extensions.stream().map(DiscoveredExtension::getDescription).toList();

    // build dependency graph
    final var dependencyGraph = createDependencyGraph(descriptions, idMapper.keySet());
    final var nodes = dependencyGraph.nodes();

    // don't waste cpu on empty graph
    if (nodes.isEmpty()) return List.of();

    final Map<String, VisitingState> visitMap = new HashMap<>();

    final List<String> sortedList = Lists.newArrayList();

    // visit all nodes and resolve dependencies
    for (final var node : nodes) {
      // no need to create a new stack
      if (visitMap.get(node) == VisitingState.VISITED) continue;

      visitNode(dependencyGraph, visitMap, node, sortedList, new Stack<>());
    }

    // map ids to there DiscoveredExtension
    return sortedList.stream()
      .map(idMapper::get)
      .toList();
  }

  private static void visitNode(
    final Graph<String> graph,
    final Map<String, VisitingState> visitMap,
    final String currentNode,
    final List<String> sortedList,
    final Stack<String> currentStack
  ) {
    switch (visitMap.getOrDefault(currentNode, VisitingState.NOT_VISITED)) {
      // nothing should happen, stack already resolved
      case VISITED -> {
        //noinspection UnnecessaryReturnStatement
        return;
      }
      // circular dependency
      case VISITING -> {
        // add this to current stack to show clearly how the dependency stack is build
        // example: dep1 -> dep2 -> dep3 -> dep1
        currentStack.push(currentNode);
        final var stack = String.join(" -> ", currentStack);
        throw new CircularDependencyException(stack);
      }
      // stack has to be discovered
      case NOT_VISITED -> {
        // add to stack to comprehend circular dependencies
        currentStack.push(currentNode);

        // mark as visiting to detect circular dependencies
        visitMap.put(currentNode, VisitingState.VISITING);

        for (final var dependency : graph.successors(currentNode)) {
          visitNode(graph, visitMap, dependency, sortedList, currentStack);
        }

        // visited
        visitMap.put(currentNode, VisitingState.VISITED);

        // clean up
        currentStack.pop();
        sortedList.add(currentNode);
      }
      default -> throw new AssertionError("Not implemented yet.");
    }
  }

  private static MutableGraph<String> createDependencyGraph(final @NotNull Collection<ExtensionDescription> extensionDescriptions,
                                                            final @NotNull Collection<String> presentedIds) {
    final MutableGraph<String> dependencyGraph = GraphBuilder.directed().expectedNodeCount(extensionDescriptions.size()).build();

    // add every id to the graph
    for (final var description : extensionDescriptions) {
      try {
        final var dependencies = description.dependencies();

        // don't waste cpu, but add itself
        // MutableGraph<E>#putEdge(E, E) adds missing nodes itself
        if (dependencies.isEmpty()) {
          dependencyGraph.addNode(description.id());
          continue;
        }

        // add all dependencies
        for (final var dep : description.dependencies()) {
          // if optional dependency is missing continue
          if (!presentedIds.contains(dep.id())) {
            // if required throw an error
            if (dep.required()) throw new DependencyNotFoundException(description.id(), dep.id());
            continue;
          }

          // anything succeed
          dependencyGraph.putEdge(description.id(), dep.id());
        }

        // catch it inside the loop
        // -> only current extension is skipped
      } catch (final DependencyNotFoundException e) {
        LOGGER.error("Error while creating Dependency Graph for %s".formatted(description.id()), e);
      }
    }

    return dependencyGraph;
  }

  private enum VisitingState {

    NOT_VISITED,
    VISITING,
    VISITED

  }

}
