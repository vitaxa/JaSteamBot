package com.vitaxa.jasteambot.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.FileVisitResult.CONTINUE;

public final class IOHelper {
    // Charset
    public static final Charset UNICODE_CHARSET = StandardCharsets.UTF_8;
    public static final Charset ASCII_CHARSET = StandardCharsets.US_ASCII;

    // Constants
    public static final int SOCKET_TIMEOUT = VerifyHelper.verifyInt(
            Integer.parseUnsignedInt(System.getProperty("jasteam.socketTimeout", Integer.toString(60000))),
            VerifyHelper.POSITIVE, "jasteam.socketTimeout can't be <= 0");
    public static final int HTTP_TIMEOUT = VerifyHelper.verifyInt(
            Integer.parseUnsignedInt(System.getProperty("jasteam.httpTimeout", Integer.toString(5000))),
            VerifyHelper.POSITIVE, "jasteam.httpTimeout can't be <= 0");
    public static final int BUFFER_SIZE = VerifyHelper.verifyInt(
            Integer.parseUnsignedInt(System.getProperty("jasteam.bufferSize", Integer.toString(4096))),
            VerifyHelper.POSITIVE, "jasteam.bufferSize can't be <= 0");

    // Paths
    public static final Path JVM_DIR = Paths.get(System.getProperty("java.home"));
    public static final Path WORKING_DIR = Paths.get(System.getProperty("user.dir"));
    public static final Path HOME_DIR = Paths.get(System.getProperty("user.home"));
    public static final Path TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

    // Platform-dependent
    public static final String CROSS_SEPARATOR = "/";
    public static final FileSystem FS = FileSystems.getDefault();
    public static final String PLATFORM_SEPARATOR = FS.getSeparator();
    public static final String OS = System.getProperty("os.name");
    // JVM
    public static final Runtime RUNTIME = Runtime.getRuntime();
    // File options
    private static final LinkOption[] LINK_OPTIONS = {};
    private static final OpenOption[] READ_OPTIONS = {StandardOpenOption.READ};
    private static final CopyOption[] COPY_OPTIONS = {StandardCopyOption.REPLACE_EXISTING};
    private static final OpenOption[] APPEND_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND};
    private static final OpenOption[] WRITE_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};
    private static final Set<FileVisitOption> WALK_OPTIONS = Collections.singleton(FileVisitOption.FOLLOW_LINKS);

    private IOHelper() {
    }

    public static void close(AutoCloseable closeable) {
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }


    public static void copy(Path source, Path target) throws IOException {
        createParentDirs(target);
        Files.copy(source, target, COPY_OPTIONS);
    }

    public static void copy(String source, String target) throws IOException {
        Path dest = Paths.get(target);
        Path src = Paths.get(source);
        createParentDirs(dest);
        Files.copy(src, dest, COPY_OPTIONS);
    }


    public static void createParentDirs(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !isDir(parent)) {
            Files.createDirectories(parent);
        }
    }

    public static void createDir(Path dir) throws IOException {
        if (!isDir(dir)) {
            Files.createDirectory(dir);
        }
    }

    public static String decode(byte[] bytes) {
        return new String(bytes, UNICODE_CHARSET);
    }


    public static String decodeASCII(byte[] bytes) {
        return new String(bytes, ASCII_CHARSET);
    }


    public static void deleteDir(Path dir, boolean self) throws IOException {
        walk(dir, new DeleteDirVisitor(dir, self));
    }

    public static byte[] encode(String s) {
        return s.getBytes(UNICODE_CHARSET);
    }


    public static byte[] encodeASCII(String s) {
        return s.getBytes(ASCII_CHARSET);
    }


    public static boolean exists(Path path) {
        return Files.exists(path, LINK_OPTIONS);
    }

    public static String getFileName(Path path) {
        return path.getFileName().toString();
    }

    public static byte[] getResourceBytes(String name) throws IOException {
        return read(getResourceURL(name));
    }

    public static Path getCodeSource(Class<?> clazz) {
        return Paths.get(toURI(clazz.getProtectionDomain().getCodeSource().getLocation()));
    }

    public static URL getResourceURL(String name) throws NoSuchFileException {
        URL url = ClassLoader.getSystemResource(name);
        if (url == null) {
            throw new NoSuchFileException(name);
        }
        return url;
    }

    public static String getIP(SocketAddress address) {
        return ((InetSocketAddress) address).getAddress().getHostAddress();
    }

    public static boolean hasExtension(Path file, String extension) {
        return getFileName(file).endsWith('.' + extension);
    }


    public static boolean isDir(Path path) {
        return Files.isDirectory(path, LINK_OPTIONS);
    }


    public static boolean isEmpty(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return !stream.iterator().hasNext();
        }
    }


    public static boolean isFile(Path path) {
        return Files.isRegularFile(path, LINK_OPTIONS);
    }


    public static boolean isValidFileName(String fileName) {
        return !fileName.equals(".") && !fileName.equals("..") &&
                fileName.chars().noneMatch(ch -> ch == '/' || ch == '\\') && isValidPath(fileName);
    }


    public static boolean isValidPath(String path) {
        try {
            toPath(path);
            return true;
        } catch (InvalidPathException ignored) {
            return false;
        }
    }

    public static void move(Path source, Path target) throws IOException {
        createParentDirs(target);
        Files.move(source, target, COPY_OPTIONS);
    }


    public static byte[] newBuffer() {
        return new byte[4096];
    }


    public static ByteArrayOutputStream newByteArrayOutput() {
        return new ByteArrayOutputStream();
    }


    public static char[] newCharBuffer() {
        return new char[4096];
    }

    public static Socket newSocket() throws SocketException {
        Socket socket = new Socket();
        setSocketFlags(socket);
        return socket;
    }

    public static InputStream newInput(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        if (connection instanceof HttpURLConnection) {
            connection.setReadTimeout(HTTP_TIMEOUT);
            connection.setConnectTimeout(HTTP_TIMEOUT);
        }
        connection.setDoInput(true);
        connection.setDoOutput(false);
        return connection.getInputStream();
    }


    public static InputStream newInput(Path file) throws IOException {
        return Files.newInputStream(file, READ_OPTIONS);
    }


    public static OutputStream newOutput(Path file) throws IOException {
        return newOutput(file, false);
    }

    public static OutputStream newOutput(Path file, boolean append) throws IOException {
        createParentDirs(file);
        return Files.newOutputStream(file, append ? APPEND_OPTIONS : WRITE_OPTIONS);
    }

    public static BufferedReader newReader(InputStream input) {
        return newReader(input, UNICODE_CHARSET);
    }


    public static BufferedReader newReader(InputStream input, Charset charset) {
        return new BufferedReader(new InputStreamReader(input, charset));
    }


    public static BufferedReader newReader(URL url) throws IOException {
        return newReader(newInput(url));
    }


    public static BufferedReader newReader(Path file) throws IOException {
        return Files.newBufferedReader(file, UNICODE_CHARSET);
    }


    public static BufferedWriter newWriter(OutputStream output) {
        return new BufferedWriter(new OutputStreamWriter(output, UNICODE_CHARSET));
    }


    public static BufferedWriter newWriter(Path file) throws IOException {
        return newWriter(file, false);
    }


    public static BufferedWriter newWriter(Path file, boolean append) throws IOException {
        createParentDirs(file);
        return Files.newBufferedWriter(file, UNICODE_CHARSET, append ? APPEND_OPTIONS : WRITE_OPTIONS);
    }


    public static BufferedWriter newWriter(FileDescriptor fd) {
        return newWriter(new FileOutputStream(fd));
    }

    public static ZipEntry newZipEntry(String name) {
        ZipEntry entry = new ZipEntry(name);
        entry.setTime(0);
        return entry;
    }


    public static ZipEntry newZipEntry(ZipEntry entry) {
        return newZipEntry(entry.getName());
    }

    public static ZipOutputStream newZipOutput(FileOutputStream output) {
        return new ZipOutputStream(output);
    }

    public static byte[] read(Path file) throws IOException {
        long size = readAttributes(file).size();
        if (size > Integer.MAX_VALUE) {
            throw new IOException("File too big");
        }

        // Read bytes from file
        byte[] bytes = new byte[(int) size];
        try (InputStream input = newInput(file)) {
            read(input, bytes);
        }

        // Return result
        return bytes;
    }


    public static byte[] read(URL url) throws IOException {
        try (InputStream input = newInput(url)) {
            return read(input);
        }
    }


    public static void read(InputStream input, byte[] bytes) throws IOException {
        int offset = 0;
        while (offset < bytes.length) {
            int length = input.read(bytes, offset, bytes.length - offset);
            if (length < 0) {
                throw new EOFException(String.format("%d bytes remaining", bytes.length - offset));
            }
            offset += length;
        }
    }


    public static byte[] read(InputStream input) throws IOException {
        try (ByteArrayOutputStream output = newByteArrayOutput()) {
            transfer(input, output);
            return output.toByteArray();
        }
    }

    public static String read(BufferedReader reader) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (response.length() > 0) {
                response.append("\n");
            }
            response.append(line);
        }

        return response.toString();
    }

    public static BasicFileAttributes readAttributes(Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class, LINK_OPTIONS);
    }

    public static InetSocketAddress resolve(InetSocketAddress address) {
        if (address.isUnresolved()) { // Create resolved address
            return new InetSocketAddress(address.getHostString(), address.getPort());
        }
        return address;
    }

    public static Path toPath(String path) {
        return Paths.get(path.replace(PLATFORM_SEPARATOR, CROSS_SEPARATOR));
    }


    public static String toString(Path path) {
        return path.toString().replace(PLATFORM_SEPARATOR, CROSS_SEPARATOR);
    }

    public static URI toURI(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String removeExtension(String s) {
        String separator = System.getProperty("file.separator");
        String filename;

        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1) {
            return filename;
        }
        return filename.substring(0, extensionIndex);
    }

    public static Path searchFile(Path dir, String fileName) throws IOException {
        FileSearchNameDirVisitor search = new FileSearchNameDirVisitor(fileName);
        Files.walkFileTree(dir, WALK_OPTIONS, Integer.MAX_VALUE, search);
        return search.matchedFile;
    }

    public static void setSocketFlags(Socket socket) throws SocketException {
        // Set socket flags
        socket.setKeepAlive(false);
        socket.setTcpNoDelay(false);
        socket.setReuseAddress(true);

        // Set socket options
        socket.setSoTimeout(SOCKET_TIMEOUT);
        socket.setTrafficClass(0b11100);
        socket.setPerformancePreferences(1, 0, 2);
    }

    public static void setAttribute(Path file, String attribute, String value) throws IOException {
        Files.setAttribute(file, attribute, value, LINK_OPTIONS);
    }

    public static void setAttribute(Path file, String attribute, FileTime time) throws IOException {
        Files.setAttribute(file, attribute, time, LINK_OPTIONS);
    }

    public static int transfer(InputStream input, OutputStream output) throws IOException {
        int transferred = 0;
        byte[] buffer = newBuffer();
        for (int length = input.read(buffer); length >= 0; length = input.read(buffer)) {
            output.write(buffer, 0, length);
            transferred += length;
        }
        return transferred;
    }


    public static void transfer(Path file, OutputStream output) throws IOException {
        try (InputStream input = newInput(file)) {
            transfer(input, output);
        }
    }


    public static int transfer(InputStream input, Path file) throws IOException {
        return transfer(input, file, false);
    }


    public static int transfer(InputStream input, Path file, boolean append) throws IOException {
        try (OutputStream output = newOutput(file, append)) {
            return transfer(input, output);
        }
    }

    public static int verifyLength(int length, int max) throws IOException {
        if (length < 0 || max < 0 && length != -max || max > 0 && length > max) {
            throw new IOException("Illegal length: " + length);
        }
        return length;
    }

    public static void walk(Path dir, FileVisitor<Path> visitor) throws IOException {
        Files.walkFileTree(dir, WALK_OPTIONS, Integer.MAX_VALUE, visitor);
    }

    public static void write(Path file, byte[] bytes) throws IOException {
        createParentDirs(file);
        Files.write(file, bytes, WRITE_OPTIONS);
    }

    public static void write(Path file, byte[] bytes, Set<PosixFilePermission> perms) throws IOException {
        createParentDirs(file);
        Files.write(file, bytes, WRITE_OPTIONS);
        if (perms != null && !OS.startsWith("Windows")) {
            Files.setPosixFilePermissions(file, perms);
        }
    }

    public static void write(Path file, String value) throws IOException {
        createParentDirs(file);
        write(file.toFile(), value);
    }

    public static void write(File file, String value) throws IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(value);
        }
    }

    public static void writeAppend(Path file, byte[] bytes) throws IOException {
        createParentDirs(file);
        Files.write(file, bytes, APPEND_OPTIONS);
    }

    public static void openExplorerDir(String dir) throws IOException {
        Desktop.getDesktop().open(new File(dir));
    }

    private static final class DeleteDirVisitor extends SimpleFileVisitor<Path> {
        private final Path dir;
        private final boolean self;

        private DeleteDirVisitor(Path dir, boolean self) {
            this.dir = dir;
            this.self = self;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            FileVisitResult result = super.postVisitDirectory(dir, exc);
            if (self || !this.dir.equals(dir)) {
                Files.delete(dir);
            }
            return result;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return super.visitFile(file, attrs);
        }
    }

    private static final class FileSearchNameDirVisitor extends SimpleFileVisitor<Path> {

        private static final Logger LOG = LoggerFactory.getLogger(FileSearchNameDirVisitor.class);

        private String fileName;
        private Path matchedFile;

        public FileSearchNameDirVisitor(String fileName) {
            this.fileName = fileName;
        }

        private void match(Path file) {
            Path name = file.getFileName();
            if (name != null && name.toString().equals(fileName)) {
                matchedFile = file;
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            match(file);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
            LOG.error("Visit file failed", e);
            return CONTINUE;
        }

        public Path getMatchedFile() {
            return matchedFile;
        }
    }

}


