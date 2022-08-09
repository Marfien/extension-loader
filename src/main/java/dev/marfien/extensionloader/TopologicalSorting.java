package dev.marfien.extensionloader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import dev.marfien.extensionloader.description.ExtensionDescription;
import dev.marfien.extensionloader.exception.CircularDependencyException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
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
    final var descriptionMapper = Maps.uniqueIndex(extensions, e -> e.getDescription().id());
    final var descriptions = extensions.stream().map(DiscoveredExtension::getDescription).toList();

    // build dependency graph
    final var dependencyGraph = createDependencyGraph(descriptions);
    final var visitMap = Maps.toMap(dependencyGraph.nodes(), ignored -> VisitingState.NOT_VISITED);

    final List<String> sortedList = Lists.newArrayList();
    for (final var node : dependencyGraph.nodes()) {
      visitNode(dependencyGraph, visitMap, node, sortedList, new Stack<>());
    }

    return sortedList.stream().map(descriptionMapper::get).toList();
  }

  private static <T> void visitNode(
    final Graph<T> graph,
    final Map<T, VisitingState> visitMap,
    final T currentNode,
    final List<T> sortedList,
    final Stack<T> currentStack
  ) {
    switch (visitMap.get(currentNode)) {
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
        final var stack = currentStack.stream().map(T::toString).collect(Collectors.joining(" -> "));
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

  private static MutableGraph<String> createDependencyGraph(final @NotNull Collection<ExtensionDescription> extensionDescriptions) {
    final MutableGraph<String> dependencyGraph = GraphBuilder.directed().expectedNodeCount(extensionDescriptions.size()).build();
    extensionDescriptions.forEach(description -> {
      dependencyGraph.addNode(description.id());
      description.dependencies().forEach(dep -> dependencyGraph.putEdge(description.id(), dep.id()));
    });

    return dependencyGraph;
  }

  private enum VisitingState {

    NOT_VISITED,
    VISITING,
    VISITED

  }

}
