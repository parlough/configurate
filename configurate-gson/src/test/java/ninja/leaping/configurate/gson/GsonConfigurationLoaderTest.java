/**
 * Configurate
 * Copyright (C) zml and Configurate contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ninja.leaping.configurate.gson;

import com.google.common.io.Resources;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.loader.AtomicFiles;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.util.MapFactories;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

/**
 * Basic sanity checks for the loader
 */
public class GsonConfigurationLoaderTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testSimpleLoading() throws IOException {
        URL url = getClass().getResource("/example.json");
        final Path tempFile = folder.newFile().toPath();
        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream())))
                .setSink(AtomicFiles.createAtomicWriterFactory(tempFile, UTF_8)).setLenient(true).build();
        ConfigurationNode node = loader.load(loader.getDefaultOptions().setMapFactory(MapFactories.sortedNatural()));
        assertEquals("unicorn", node.getNode("test", "op-level").getValue());
        assertEquals("dragon", node.getNode("other", "op-level").getValue());
        assertEquals("dog park", node.getNode("other", "location").getValue());
        assertTrue(node.getNode("int-val").getValue() instanceof Integer);
        assertTrue(node.getNode("double-val").getValue() instanceof Double);
        loader.save(node);
        assertEquals(Resources.readLines(url, UTF_8), Files.readAllLines(tempFile, UTF_8));
    }

    @Test
    public void testSavingEmptyFile() throws IOException {
        final File tempFile = folder.newFile();
        tempFile.createNewFile();

        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setFile(tempFile)
                .build();

        ConfigurationNode n = SimpleConfigurationNode.root();
        loader.save(n);
    }

    @Test
    public void testLoadingEmptyFile() throws IOException {
        final File tempFile = folder.newFile();
        tempFile.createNewFile();

        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setFile(tempFile)
                .build();

        loader.load();
    }

    @Test
    public void testLoadingFileWithEmptyObject() throws IOException {
        URL url = getClass().getResource("/emptyObject.json");
        final Path tempFile = folder.newFile().toPath();
        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder()
                .setSource(() -> new BufferedReader(new InputStreamReader(url.openStream())))
                .setSink(AtomicFiles.createAtomicWriterFactory(tempFile, UTF_8)).setLenient(true).build();

        ConfigurationNode node = loader.load(loader.getDefaultOptions().setMapFactory(MapFactories.sortedNatural()));
        assertNull(node.getValue());
        assertFalse(node.hasMapChildren());
    }

    private static final long TEST_LONG_VAL = 584895858588588888l;

    @Test
    @Ignore("Gson currently makes it rather difficult to get the correct number type")
    public void testRoundtrippingLong() throws IOException {
        final Path tempFile = folder.newFile().toPath();
        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader.builder().setPath(tempFile).build();
        ConfigurationNode start = loader.createEmptyNode();
        start.getNode("long-num").setValue(TEST_LONG_VAL);
        loader.save(start);
        System.out.println(Files.readAllLines(tempFile));

        ConfigurationNode ret = loader.load();
        System.out.println(ret.getNode("long-num").getValue().getClass());
        assertEquals(TEST_LONG_VAL, ret.getNode("long-num").getValue());
    }
}
