/*
 * Remove @deprecated annotations from
 * target/generated-sources/javacc/com/github/jochenw/afw/core/el/jcc/SimpleCharStream.java
 */
System.out.println("Removing invalid @deprecated annotations.");
final java.nio.file.Path path =
    java.nio.file.Paths.get("${project.basedir}/target/generated-sources/javacc/com/github/jochenw/afw/core/el/jcc/SimpleCharStream.java");
if (!java.nio.file.Files.isRegularFile(path)) {
    throw new IllegalStateException("File not found: " + path);
}
List<String> lines = java.nio.file.Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8);
int numberOfLinesRemoved = 0;
for (int i = 0;  i < lines.size();) {
    String line = lines.get(i);
    if ("   * @deprecated".equals(line)) {
        lines.remove(i);
        numberOfLinesRemoved++;
	} else {
	    // Ignore this line, go to the next one.
	    i++;
	}
}
System.out.println("Number of @deprecated annotations removed: " + numberOfLinesRemoved);
java.nio.file.Files.write(path, lines, java.nio.charset.StandardCharsets.UTF_8);
