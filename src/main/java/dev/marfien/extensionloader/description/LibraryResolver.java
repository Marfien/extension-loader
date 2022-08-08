package dev.marfien.extensionloader.description;

import com.google.common.collect.Lists;
import net.minestom.dependencies.DependencyGetter;
import net.minestom.dependencies.DependencyResolver;
import net.minestom.dependencies.ResolvedDependency;
import net.minestom.dependencies.maven.MavenRepository;
import net.minestom.dependencies.maven.MavenResolver;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class LibraryResolver {

  private static final MavenResolver DEFAULT = new MavenResolver(List.of(MavenRepository.Companion.getCentral(), MavenRepository.Companion.getJCenter()));

  private final DependencyGetter getter = new DependencyGetter();
  {
    this.getter.addResolver(DEFAULT);
  }

  public void addResolver(final @NotNull DependencyResolver resolver) {
    this.getter.addResolver(resolver);
  }

  public void addRepositories(final @NotNull LibraryInfo.Repository... repositories) {
    this.addRepositories(List.of(repositories));
  }

  public void addRepositories(final @NotNull Collection<LibraryInfo.Repository> repositories) {
    this.getter.addMavenResolver(repositories.stream().map(LibraryInfo.Repository::toMaven).toList());
  }

  public Collection<ResolvedDependency> resolve(final @NotNull LibraryInfo.Artifact dependency, final @NotNull Path path) {
    final var resolved = this.getter.get(dependency.toString(), path.toFile());

    final Collection<ResolvedDependency> dependencies = Lists.newArrayList();
    addSubDependencies(resolved, dependencies);

    return dependencies;
  }

  private static void addSubDependencies(final @NotNull ResolvedDependency dependency, final @NotNull Collection<ResolvedDependency> dependencies) {
    dependencies.add(dependency);

    dependency.getSubdependencies().forEach(dep -> addSubDependencies(dep, dependencies));
  }

}
