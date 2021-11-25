package de.foellix.aql.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class HashHelperTest {
	@Test
	void getAnswerFilesAsStringTest() {
		final List<FileWithHash> list = new ArrayList<>();
		final FileWithHash file = new FileWithHash("test1.xml");
		list.add(file);
		final String actual = Helper.getAnswerFilesAsString(list);
		assertEquals(HashHelper.sha256Hash(file.getFile().getAbsolutePath(), true), HashHelper.sha256Hash(actual));
	}

	@Test
	void hashDirectoryTest() {
		assertEquals("c590b3c924c35c2f241746522284e4709df490d73a38aaa7d6de4ed1eac2f546",
				HashHelper.sha256Hash(new File("examples")));
	}
}
