package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;

import java.io.EOFException;
import java.io.IOException;

@OriginalClass("client!ge")
public final class Cache {

	@OriginalMember(owner = "client!wc", name = "i", descriptor = "[B")
	public static final byte[] buffer = new byte[520];

	@OriginalMember(owner = "client!ge", name = "a", descriptor = "Lclient!en;")
	private BufferedFile data = null;

	@OriginalMember(owner = "client!ge", name = "f", descriptor = "Lclient!en;")
	private BufferedFile index = null;

	@OriginalMember(owner = "client!ge", name = "l", descriptor = "I")
	private int maxLen = 65000;

	@OriginalMember(owner = "client!ge", name = "c", descriptor = "I")
	private final int archive;

	@OriginalMember(owner = "client!ge", name = "<init>", descriptor = "(ILclient!en;Lclient!en;I)V")
	public Cache(@OriginalArg(0) int archive, @OriginalArg(1) BufferedFile data, @OriginalArg(2) BufferedFile index, @OriginalArg(3) int maxLen) {
		this.maxLen = maxLen;
		this.index = index;
		this.archive = archive;
		this.data = data;
	}

	@OriginalMember(owner = "client!ge", name = "toString", descriptor = "()Ljava/lang/String;")
	@Override
	public final String toString() {
		return "Cache:" + this.archive;
	}

	@OriginalMember(owner = "client!ge", name = "a", descriptor = "(II[BB)Z")
	public final boolean write(@OriginalArg(0) int group, @OriginalArg(1) int len, @OriginalArg(2) byte[] data) {
		//@Pc(7) BufferedFile local7 = this.data;
		synchronized (this.data) {
			if (len < 0 || len > this.maxLen) {
				throw new IllegalArgumentException();
			}
			@Pc(35) boolean writeSuccess = this.write(len, group, data, true);
			if (!writeSuccess) {
				writeSuccess = this.write(len, group, data, false);
			}
			return writeSuccess;
		}
	}

	@OriginalMember(owner = "client!ge", name = "a", descriptor = "(IB)[B")
	public final byte[] read(@OriginalArg(0) int group) {
		//@Pc(9) BufferedFile local9 = this.data;
		synchronized (this.data) {
			try {
				// Do nothing if read is out of bounds.
				//@Pc(27) Object local27;
				@Pc(27) byte[] blankResult = null;
				if (this.index.length() < (long) (group * 6 + 6)) {
					return blankResult;
				}

				// Read requested group from index file.
				this.index.seek(group * 6);
				this.index.read(0, buffer, 6);
				
				// Parse the group info.
				@Pc(69) int currentReadIdx = ((buffer[3] & 0xFF) << 16) - (-((buffer[4] & 0xFF) << 8) - (buffer[5] & 0xFF));
				int firstReadIdx = currentReadIdx;
				@Pc(99) int resultLen = (buffer[2] & 0xFF) + ((buffer[1] & 0xFF) << 8) + ((buffer[0] & 0xFF) << 16);
				int firstResultLen = resultLen;

				// Verify group info boundary conditions.
				if (resultLen < 0 || this.maxLen < resultLen) {
					return blankResult;
				} else if (currentReadIdx <= 0 || (long) currentReadIdx > this.data.length() / 520L) {
					return blankResult;
				} else {
					// Start reading chunks of data from data file.
					@Pc(134) byte[] result = new byte[resultLen];
					@Pc(136) int currentWriteIdx = 0;
					@Pc(138) int currentChunkIdx = 0;
					while (currentWriteIdx < resultLen) {
						
						// Read index should never be zero.
						if (currentReadIdx == 0) {
							return blankResult;
						}

						// Cap reads at max length of chunk (520 bytes) minus header (8 bytes).
						@Pc(157) int numBytesForCurrentRead = resultLen - currentWriteIdx;
						this.data.seek(currentReadIdx * 520);
						if (numBytesForCurrentRead > 512) {
							numBytesForCurrentRead = 512;
						}

						// Read a chunk of data at current index.
						this.data.read(0, buffer, numBytesForCurrentRead + 8);

						// Parse the data chunk.
						@Pc(197) int groupInDataFile = ((buffer[0] & 0xFF) << 8) + (buffer[1] & 0xFF);
						@Pc(211) int chunkIdx = (buffer[3] & 0xFF) + ((buffer[2] & 0xFF) << 8);
						@Pc(217) int archive = buffer[7] & 0xFF;
						@Pc(239) int nextReadIdx = (buffer[6] & 0xFF) + ((buffer[5] & 0xFF) << 8) + ((buffer[4] & 0xFF) << 16);

						// Validate metadata in chunk matches expected values.
						if (group != groupInDataFile || currentChunkIdx != chunkIdx || this.archive != archive) {
							return blankResult;
						}

						// Verify next read index boundary conditions.
						if (nextReadIdx < 0 || (long) nextReadIdx > this.data.length() / 520L) {
							return blankResult;
						}

						// Copy bytes from data file to result buffer.
						for (@Pc(272) int i = 0; i < numBytesForCurrentRead; i++) {
							result[currentWriteIdx++] = buffer[i + 8];
						}

						currentChunkIdx++;
						currentReadIdx = nextReadIdx;
					}
					// @Pc(297) byte[] local297 = dataReadResult;
					// return local297;

					if(firstRun) {
						System.out.println("Group: " + group);
						System.out.println("First seek in data file: " + firstReadIdx * 520);
						System.out.println("Length of first data chunk: " + firstResultLen);
						firstRun = false;
					}

					return result;
				}
			} catch (@Pc(301) IOException ex) {
				ex.printStackTrace();
				return null;
			}
		}
	}

	@OriginalMember(owner = "client!ge", name = "a", descriptor = "(BII[BZ)Z")
	private boolean write(@OriginalArg(1) int dataToWriteLen, @OriginalArg(2) int group, @OriginalArg(3) byte[] dataToWrite, @OriginalArg(4) boolean overwriteExisting) {
		// @Pc(9) BufferedFile local9 = this.data;
		synchronized (this.data) {
			try {
				@Pc(67) int currentChunkIdx;
				@Pc(27) boolean writeSuccess;
				if (overwriteExisting) {
					// Validate index file boundaries.
					if (this.index.length() < (long) (group * 6 + 6)) {
						writeSuccess = false;
						return writeSuccess;
					}

					// Read entry in index file.
					this.index.seek(group * 6);
					this.index.read(0, buffer, 6);

					// Parse index file.
					currentChunkIdx = ((buffer[3] & 0xFF) << 16) + (buffer[4] << 8 & 0xFF00) + (buffer[5] & 0xFF);
					
					// Validate data file chunk boundaries.
					if (currentChunkIdx <= 0 || this.data.length() / 520L < (long) currentChunkIdx) {
						writeSuccess = false;
						return writeSuccess;
					}
				} else {
					// No overwrite - begin write at end of data file instead.
					currentChunkIdx = (int) ((this.data.length() + 519L) / 520L);
					if (currentChunkIdx == 0) {
						currentChunkIdx = 1;
					}
				}

				// Build new index file entry to match data that we will write.
				buffer[0] = (byte) (dataToWriteLen >> 16);
				buffer[4] = (byte) (currentChunkIdx >> 8);
				@Pc(125) int currentOffset = 0;
				buffer[5] = (byte) currentChunkIdx;
				buffer[2] = (byte) dataToWriteLen;
				buffer[3] = (byte) (currentChunkIdx >> 16);
				@Pc(156) int expectedChunkOrder = 0;
				buffer[1] = (byte) (dataToWriteLen >> 8);

				// Overwrite entry in index file.
				this.index.seek(group * 6);
				this.index.write(buffer, 0, 6);

				// Begin writing chunks of data to data file.
				while (true) {
					if (currentOffset < dataToWriteLen) {
						label134:
						{
							@Pc(189) int nextChunkIdx = 0;
							// @Pc(248) int local248;
							@Pc(248) int chunkGroup;
							if (overwriteExisting) {
								// Read header of existing chunk to be overwritten.
								this.data.seek(currentChunkIdx * 520);
								try {
									this.data.read(0, buffer, 8);
								} catch (@Pc(209) EOFException local209) {
									break label134;
								}

								// Parse chunk header.
								nextChunkIdx = ((buffer[4] & 0xFF) << 16) + ((buffer[5] & 0xFF) << 8) + (buffer[6] & 0xFF);
								chunkGroup = (buffer[1] & 0xFF) + ((buffer[0] & 0xFF) << 8);
								@Pc(254) int archive = buffer[7] & 0xFF;
								@Pc(268) int chunkOrder = (buffer[3] & 0xFF) + ((buffer[2] & 0xFF) << 8);

								// Validate chunk header.
								if (chunkGroup != group || expectedChunkOrder != chunkOrder || this.archive != archive) {
									writeSuccess = false;
									return writeSuccess;
								}
								if (nextChunkIdx < 0 || (long) nextChunkIdx > this.data.length() / 520L) {
									writeSuccess = false;
									return writeSuccess;
								}
							}

							// Don't write more than 512 bytes (max size of chunk including 8-byte header).
							@Pc(248) int numBytesToWrite = dataToWriteLen - currentOffset;
							
							// Next chunk index of zero indicates no next chunk.
							// In this case we want to write to end of file instead of overwriting existing.
							if (nextChunkIdx == 0) {
								overwriteExisting = false;
								nextChunkIdx = (int) ((this.data.length() + 519L) / 520L);
								if (nextChunkIdx == 0) {
									nextChunkIdx++;
								}
								if (nextChunkIdx == currentChunkIdx) {
									nextChunkIdx++;
								}
							}

							// Build header for new data chunk.
							buffer[7] = (byte) this.archive;
							buffer[0] = (byte) (group >> 8);
							if (dataToWriteLen - currentOffset <= 512) {
								nextChunkIdx = 0;
							}
							buffer[4] = (byte) (nextChunkIdx >> 16);
							if (numBytesToWrite > 512) {
								numBytesToWrite = 512;
							}
							buffer[1] = (byte) group;
							buffer[6] = (byte) nextChunkIdx;
							buffer[2] = (byte) (expectedChunkOrder >> 8);
							buffer[3] = (byte) expectedChunkOrder;
							expectedChunkOrder++;
							buffer[5] = (byte) (nextChunkIdx >> 8);

							// Write header.
							this.data.seek(currentChunkIdx * 520);
							currentChunkIdx = nextChunkIdx;
							this.data.write(buffer, 0, 8);

							// Write data chunk.
							this.data.write(dataToWrite, currentOffset, numBytesToWrite);
							currentOffset += numBytesToWrite;
							continue;
						}
					}
					writeSuccess = true;
					return writeSuccess;
				}
			} catch (@Pc(453) IOException local453) {
				return false;
			}
		}
	}
}
