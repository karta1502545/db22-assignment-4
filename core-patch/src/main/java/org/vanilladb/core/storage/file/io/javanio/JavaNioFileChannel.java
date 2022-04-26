/*******************************************************************************
 * Copyright 2016, 2017 vanilladb.org contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.vanilladb.core.storage.file.io.javanio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.vanilladb.core.storage.file.io.IoBuffer;
import org.vanilladb.core.storage.file.io.IoChannel;

import java.util.concurrent.locks.*;

public class JavaNioFileChannel implements IoChannel {

	private FileChannel fileChannel;
	private ReadWriteLock lock;

	public JavaNioFileChannel(File file) throws IOException {
		@SuppressWarnings("resource")
		RandomAccessFile f = new RandomAccessFile(file, "rws");
		fileChannel = f.getChannel();
		lock = new ReentrantReadWriteLock();
	}

	@Override
	public int read(IoBuffer buffer, long position) throws IOException {
		lock.readLock().lock();
		try {
			JavaNioByteBuffer javaBuffer = (JavaNioByteBuffer) buffer;
			return fileChannel.read(javaBuffer.getByteBuffer(), position);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public int write(IoBuffer buffer, long position) throws IOException {
		lock.writeLock().lock();
		try {
			JavaNioByteBuffer javaBuffer = (JavaNioByteBuffer) buffer;
			return fileChannel.write(javaBuffer.getByteBuffer(), position);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public long append(IoBuffer buffer) throws IOException {
		lock.writeLock().lock();
		try {
			JavaNioByteBuffer javaBuffer = (JavaNioByteBuffer) buffer;
			fileChannel.write(javaBuffer.getByteBuffer(), size());
			return size();
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public long size() throws IOException {
		lock.readLock().lock();
		try {
			return fileChannel.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void close() throws IOException {
		lock.writeLock().lock();
		try {
			fileChannel.close();
		} finally {
			lock.writeLock().unlock();
		}
	}
}
