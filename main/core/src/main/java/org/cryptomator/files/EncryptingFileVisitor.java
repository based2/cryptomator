package org.cryptomator.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.cryptomator.crypto.Cryptor;
import org.cryptomator.crypto.CryptorIOSupport;

public class EncryptingFileVisitor extends SimpleFileVisitor<Path> implements CryptorIOSupport {

	private final Path rootDir;
	private final Cryptor cryptor;
	private final EncryptionDecider encryptionDecider;
	private Path currentDir;

	public EncryptingFileVisitor(Path rootDir, Cryptor cryptor, EncryptionDecider encryptionDecider) {
		this.rootDir = rootDir;
		this.cryptor = cryptor;
		this.encryptionDecider = encryptionDecider;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if (rootDir.equals(dir) || encryptionDecider.shouldEncrypt(dir)) {
			this.currentDir = dir;
			return FileVisitResult.CONTINUE;
		} else {
			return FileVisitResult.SKIP_SUBTREE;
		}
	}

	@Override
	public FileVisitResult visitFile(Path plaintextFile, BasicFileAttributes attrs) throws IOException {
		if (encryptionDecider.shouldEncrypt(plaintextFile)) {
			final String plaintextName = plaintextFile.getFileName().toString();
			final String encryptedName = cryptor.encryptPath(plaintextName, '/', '/', this);
			final Path encryptedPath = plaintextFile.resolveSibling(encryptedName);
			final InputStream plaintextIn = Files.newInputStream(plaintextFile, StandardOpenOption.READ);
			final SeekableByteChannel ciphertextOut = Files.newByteChannel(encryptedPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
			cryptor.encryptFile(plaintextIn, ciphertextOut);
			Files.delete(plaintextFile);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		if (encryptionDecider.shouldEncrypt(dir)) {
			final String plaintext = dir.getFileName().toString();
			final String encrypted = cryptor.encryptPath(plaintext, '/', '/', this);
			final Path newPath = dir.resolveSibling(encrypted);
			Files.move(dir, newPath, StandardCopyOption.ATOMIC_MOVE);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public void writePathSpecificMetadata(String metadataFile, byte[] encryptedMetadata) throws IOException {
		final Path path = currentDir.resolve(metadataFile);
		Files.write(path, encryptedMetadata, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.DSYNC);
	}

	@Override
	public byte[] readPathSpecificMetadata(String metadataFile) throws IOException {
		final Path path = currentDir.resolve(metadataFile);
		return Files.readAllBytes(path);
	}

	/* callback */

	public interface EncryptionDecider {
		boolean shouldEncrypt(Path path);
	}

}
