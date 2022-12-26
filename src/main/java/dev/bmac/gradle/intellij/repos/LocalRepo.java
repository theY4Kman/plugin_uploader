package dev.bmac.gradle.intellij.repos;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class LocalRepo extends Repo {
    private final Path basePath;

    public LocalRepo(String baseRepoPath, String authentication) {
        super(baseRepoPath, authentication);
        basePath = Paths.get(baseRepoPath).toAbsolutePath();
    }

    private Path getBasePath() throws IOException {
        if (!basePath.toFile().exists()) {
            throw new IOException("Base path does not exist: " + basePath);
        }
        return basePath;
    }

    @Override
    public <T> T get(String relativePath, Function<RepoObject, T> converter) throws IOException {
        final File file = basePath.resolve(relativePath).toFile();
        if (!file.exists()) {
            return converter.apply(RepoObject.empty());
        }

        final InputStream stream = new FileInputStream(file);
        return converter.apply(RepoObject.of(stream));
    }

    @Override
    public void upload(String relativePath, File file, String mediaType) throws IOException {
        final Path path = getBasePath().resolve(relativePath);
        final Path parent = path.getParent();
        if (!parent.equals(basePath) && !parent.toFile().exists()) {
            if (!parent.toFile().mkdirs()) {
                throw new IOException("Could not create parent directories for " + path);
            }
        }

        try (var in = new FileInputStream(file); var out = new FileOutputStream(path.toFile())) {
            in.transferTo(out);
        }
    }

    @Override
    public void delete(String relativePath) throws IOException {
        final File file = basePath.resolve(relativePath).toFile();
        if (file.exists()) {
            file.delete();
        }
    }
}
