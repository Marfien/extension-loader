package dev.marfien.extensionloader.annotation;

import org.jetbrains.annotations.ApiStatus;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;

@ApiStatus.Internal
@SupportedAnnotationTypes("dev.marfien.extensionloader.annotation.ExtensionMeta")
public class AnnotationProcessor extends AbstractProcessor {

  private String foundClass;

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    if (annotations.isEmpty()) return false;
    if (roundEnv.processingOver()) return false;

    if (this.foundClass != null) {
      this.printDoubleWarning();
      return false;
    }

    for (final var element : roundEnv.getElementsAnnotatedWith(ExtensionMeta.class)) {
      if (element.getKind() != ElementKind.CLASS) {
        super.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@ExtensionMeta is only allowed on classes.");
        return false;
      }

      if (this.foundClass != null) {
        this.printDoubleWarning();
        return false;
      }

      if (!(element instanceof TypeElement typeElement)) throw new AssertionError();

      final var name = typeElement.getQualifiedName();
      this.foundClass = name.toString();

      final var fileName = typeElement.getAnnotation(EntryPointFileName.class);

      try {
        final var file = super.processingEnv.getFiler()
          .createResource(StandardLocation.CLASS_OUTPUT, "", fileName != null ? fileName.value() : "entrypoint.extension");
        try (final var writer = new BufferedWriter(file.openWriter())) {
            writer.append(this.foundClass);
        }
      } catch (final IOException e) {
        super.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to create file: %s".formatted(e.getMessage()));
      }
    }

    return false;
  }

  private void printDoubleWarning() {
    super.processingEnv.getMessager()
      .printMessage(Diagnostic.Kind.WARNING, "Currently, only one extension per file is allowed. Using: %s".formatted(this.foundClass));
  }

}
