package com.google.gcloud.storage.contrib.nio;

import static com.google.common.truth.Truth.assertThat;
import static com.google.gcloud.storage.contrib.nio.CloudStorageOptions.withCacheControl;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.gcloud.storage.testing.LocalGcsHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

/**
 * Unit tests for {@link CloudStorageFileAttributeView}.
 */
@RunWith(JUnit4.class)
public class CloudStorageFileAttributeViewTest {

  private static final byte[] HAPPY = "(✿◕ ‿◕ )ノ".getBytes(UTF_8);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private Path path;

  @Before
  public void before() {
    CloudStorageFileSystemProvider.setGCloudOptions(LocalGcsHelper.options());
    path = Paths.get(URI.create("gs://red/water"));
  }

  @Test
  public void testReadAttributes() throws IOException {
    Files.write(path, HAPPY, withCacheControl("potato"));
    CloudStorageFileAttributeView lazyAttributes =
        Files.getFileAttributeView(path, CloudStorageFileAttributeView.class);
    assertThat(lazyAttributes.readAttributes().cacheControl().get()).isEqualTo("potato");
  }

  @Test
  public void testReadAttributes_notFound_throwsNoSuchFileException() throws IOException {
    CloudStorageFileAttributeView lazyAttributes =
        Files.getFileAttributeView(path, CloudStorageFileAttributeView.class);
    thrown.expect(NoSuchFileException.class);
    lazyAttributes.readAttributes();
  }

  @Test
  public void testReadAttributes_pseudoDirectory() throws IOException {
    Path dir = Paths.get(URI.create("gs://red/rum/"));
    CloudStorageFileAttributeView lazyAttributes =
        Files.getFileAttributeView(dir, CloudStorageFileAttributeView.class);
    assertThat(lazyAttributes.readAttributes())
        .isInstanceOf(CloudStoragePseudoDirectoryAttributes.class);
  }

  @Test
  public void testName() throws IOException {
    Files.write(path, HAPPY, withCacheControl("potato"));
    CloudStorageFileAttributeView lazyAttributes =
        Files.getFileAttributeView(path, CloudStorageFileAttributeView.class);
    assertThat(lazyAttributes.name()).isEqualTo("gcs");
  }

  @Test
  public void testEquals_equalsTester() {
    new EqualsTester()
        .addEqualityGroup(
            Files.getFileAttributeView(
                Paths.get(URI.create("gs://red/rum")), CloudStorageFileAttributeView.class),
            Files.getFileAttributeView(
                Paths.get(URI.create("gs://red/rum")), CloudStorageFileAttributeView.class))
        .addEqualityGroup(
            Files.getFileAttributeView(
                Paths.get(URI.create("gs://red/lol/dog")), CloudStorageFileAttributeView.class))
        .testEquals();
  }

  @Test
  public void testNullness() throws NoSuchMethodException, SecurityException {
    new NullPointerTester()
        .ignore(CloudStorageFileAttributeView.class.getMethod("equals", Object.class))
        .setDefault(FileTime.class, FileTime.fromMillis(0))
        .testAllPublicInstanceMethods(
            Files.getFileAttributeView(path, CloudStorageFileAttributeView.class));
  }
}
