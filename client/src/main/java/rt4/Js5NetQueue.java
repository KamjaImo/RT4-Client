package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;

import java.io.IOException;

@OriginalClass("client!jb")
public final class Js5NetQueue {

	@OriginalMember(owner = "client!jb", name = "A", descriptor = "J")
	private long previousLoop;

	@OriginalMember(owner = "client!jb", name = "B", descriptor = "Lclient!ma;")
	private BufferedSocket socket;

	@OriginalMember(owner = "client!jb", name = "C", descriptor = "I")
	private int latency;

	@OriginalMember(owner = "client!jb", name = "J", descriptor = "Lclient!pm;")
	private Js5NetRequest current;

	@OriginalMember(owner = "client!jb", name = "a", descriptor = "Lclient!ce;")
	private final SecondaryLinkedList pendingUrgentRequests = new SecondaryLinkedList();

	@OriginalMember(owner = "client!jb", name = "q", descriptor = "Lclient!ce;")
	private final SecondaryLinkedList inFlightUrgentRequests = new SecondaryLinkedList();

	@OriginalMember(owner = "client!jb", name = "v", descriptor = "Lclient!ce;")
	private final SecondaryLinkedList pendingPrefetchRequests = new SecondaryLinkedList();

	@OriginalMember(owner = "client!jb", name = "z", descriptor = "Lclient!ce;")
	private final SecondaryLinkedList inFlightPrefetchRequests = new SecondaryLinkedList();

	@OriginalMember(owner = "client!jb", name = "E", descriptor = "Lclient!wa;")
	private final Buffer outBuffer = new Buffer(4);

	@OriginalMember(owner = "client!jb", name = "G", descriptor = "B")
	private byte encryptionKey = 0;

	@OriginalMember(owner = "client!jb", name = "I", descriptor = "I")
	public volatile int errors = 0;

	@OriginalMember(owner = "client!jb", name = "H", descriptor = "I")
	public volatile int response = 0;

	@OriginalMember(owner = "client!jb", name = "F", descriptor = "Lclient!wa;")
	private final Buffer inBuffer = new Buffer(8);

	@OriginalMember(owner = "client!jb", name = "a", descriptor = "(I)Z")
	public final boolean isPrefetchRequestQueueFull() {
		return this.getPrefetchRequestCount() >= 20;
	}

	@OriginalMember(owner = "client!jb", name = "b", descriptor = "(B)Z")
	public final boolean loop() {
		@Pc(19) int local19;
		if (this.socket != null) {
			@Pc(12) long local12 = MonotonicClock.currentTimeMillis();
			local19 = (int) (local12 - this.previousLoop);
			this.previousLoop = local12;
			if (local19 > 200) {
				local19 = 200;
			}
			this.latency += local19;
			if (this.latency > 30000) {
				try {
					this.socket.close();
				} catch (@Pc(43) Exception local43) {
				}
				this.socket = null;
			}
		}
		if (this.socket == null) {
			return this.getUrgentRequestCount() == 0 && this.getPrefetchRequestCount() == 0;
		}
		try {
			this.socket.checkError();

			// Send a pre-request to the server containing the priority and request key.
			@Pc(75) Js5NetRequest currentRequest;
			for (currentRequest = (Js5NetRequest) this.pendingUrgentRequests.head(); currentRequest != null; currentRequest = (Js5NetRequest) this.pendingUrgentRequests.next()) {
				this.outBuffer.offset = 0;
				this.outBuffer.p1(1);
				this.outBuffer.p3((int) currentRequest.secondaryKey);
				this.socket.write(this.outBuffer.data, 4);
				this.inFlightUrgentRequests.addTail(currentRequest);
			}
			for (currentRequest = (Js5NetRequest) this.pendingPrefetchRequests.head(); currentRequest != null; currentRequest = (Js5NetRequest) this.pendingPrefetchRequests.next()) {
				this.outBuffer.offset = 0;
				this.outBuffer.p1(0);
				this.outBuffer.p3((int) currentRequest.secondaryKey);
				this.socket.write(this.outBuffer.data, 4);
				this.inFlightPrefetchRequests.addTail(currentRequest);
			}

			// Process response data as it becomes available, but stop after 100 iterations to avoid hanging
			for (@Pc(172) int iter = 0; iter < 100; iter++) {

				// Check how much data is available from the stream
				int bytesAvailableFromStream = this.socket.available();
				if (bytesAvailableFromStream < 0) {
					throw new IOException();
				}
				if (bytesAvailableFromStream == 0) {
					break; // No data to process, so we're done!
				}

				this.latency = 0;
				@Pc(196) byte inBufferLen = 0;
				if (this.current == null) {
					// If this.current is null, that means a new request is ready to be initiated.
					// Thus, the next read should be 8 bytes (size of pre-request response).
					inBufferLen = 8;	
				} else if (this.current.blockPosition == 0) {
					// If this.current is non-null and block position is zero, 
					// that means a block boundary was crossed within a request.
					// Thus, the next read should be 1 byte (to determine if there is more data in request or not).
					inBufferLen = 1;
				}

				// If current read operation is mid-block 
				@Pc(228) int usableDataLen;
				@Pc(235) int bytesToRead;
				@Pc(283) int currentByte;
				if (inBufferLen <= 0) {

					// Read as much data from the stream as possible
					usableDataLen = this.current.buffer.data.length - this.current.trailerLen;
					bytesToRead = 512 - this.current.blockPosition;
					if (bytesToRead > usableDataLen - this.current.buffer.offset) {
						bytesToRead = usableDataLen - this.current.buffer.offset;
					}
					if (bytesToRead > bytesAvailableFromStream) {
						bytesToRead = bytesAvailableFromStream;
					}
					this.socket.read(this.current.buffer.offset, bytesToRead, this.current.buffer.data);

					// Decrypt data by ORing each byte with encryption key
					if (this.encryptionKey != 0) {
						for (currentByte = 0; currentByte < bytesToRead; currentByte++) {
							this.current.buffer.data[this.current.buffer.offset + currentByte] = (byte) (this.current.buffer.data[this.current.buffer.offset + currentByte] ^ this.encryptionKey);
						}
					}

					// If all the data has been retrieved, mark the request as done and remove from queue
					this.current.blockPosition += bytesToRead;
					this.current.buffer.offset += bytesToRead;
					if (this.current.buffer.offset == usableDataLen) {
						this.current.unlinkSecondary();
						this.current.incomplete = false;
						this.current = null;
					} 
					
					// Reset block position if it crosses boundary (block size 512 bytes)
					else if (this.current.blockPosition == 512) {
						this.current.blockPosition = 0;
					}
				} 
				
				// If current read operation is at the start of a block
				else {

					// Read pre-request response from server
					usableDataLen = inBufferLen - this.inBuffer.offset;
					if (bytesAvailableFromStream < usableDataLen) {
						usableDataLen = bytesAvailableFromStream;
					}
					this.socket.read(this.inBuffer.offset, usableDataLen, this.inBuffer.data);

					// Decrypt data by ORing each byte with encryption key
					if (this.encryptionKey != 0) {
						for (bytesToRead = 0; bytesToRead < usableDataLen; bytesToRead++) {
							this.inBuffer.data[bytesToRead + this.inBuffer.offset] ^= this.encryptionKey;
						}
					}

					// Repeat until the whole input buffer is read
					this.inBuffer.offset += usableDataLen;
					if (this.inBuffer.offset >= inBufferLen) {

						// Start of new request
						if (this.current == null) {
							this.inBuffer.offset = 0;

							// Parse the pre-request response
							//bytesToRead = this.inBuffer.g1();
							//currentByte = this.inBuffer.g2();
							int requestKeyHigh = this.inBuffer.g1();
							int requestKeyLow = this.inBuffer.g2();
							@Pc(471) int local471 = this.inBuffer.g1();
							@Pc(476) int contentLength = this.inBuffer.g4();
							@Pc(480) int local480 = local471 & 0x7F;
							@Pc(491) boolean isPrefetch = (local471 & 0x80) != 0;
							@Pc(501) long requestKey = (requestKeyHigh << 16) + requestKeyLow;

							// Match the request key in the pre-request response to one of the requests in the queue
							@Pc(509) Js5NetRequest matchingRequest;
							if (isPrefetch) {
								for (matchingRequest = (Js5NetRequest) this.inFlightPrefetchRequests.head(); matchingRequest != null && matchingRequest.secondaryKey != requestKey; matchingRequest = (Js5NetRequest) this.inFlightPrefetchRequests.next()) {
								}
							} else {
								for (matchingRequest = (Js5NetRequest) this.inFlightUrgentRequests.head(); matchingRequest != null && requestKey != matchingRequest.secondaryKey; matchingRequest = (Js5NetRequest) this.inFlightUrgentRequests.next()) {
								}
							}

							// Unable to find a matching request
							if (matchingRequest == null) {
								throw new IOException();
							}

							// Initialize current request using buffer size(s) specified in response
							@Pc(568) int padding = local480 == 0 ? 5 : 9;
							this.current = matchingRequest;
							this.current.buffer = new Buffer(contentLength + padding + this.current.trailerLen);
							this.current.buffer.p1(local480);
							this.current.buffer.p4(contentLength);
							this.current.blockPosition = 8;
							this.inBuffer.offset = 0;
						} 
						
						// This condition should not be reachable
						else if (this.current.blockPosition != 0) {
							throw new IOException();
						} 
						
						// Start of new block in existing request
						else if (this.inBuffer.data[0] == -1) {
							this.current.blockPosition = 1;
							this.inBuffer.offset = 0;
						} 
						
						// End of existing request and start of new one
						else {
							this.current = null;
						}
					}
				}
			}
			return true;
		} catch (@Pc(644) IOException ioex) {
			try {
				this.socket.close();
			} catch (@Pc(650) Exception ignored) {
			}
			this.response = -2;
			this.errors++;
			this.socket = null;
			return this.getUrgentRequestCount() == 0 && this.getPrefetchRequestCount() == 0;
		}
	}

	@OriginalMember(owner = "client!jb", name = "a", descriptor = "(Z)V")
	public final void drop() {
		if (this.socket == null) {
			return;
		}
		try {
			this.outBuffer.offset = 0;
			this.outBuffer.p1(7);
			this.outBuffer.p3(0);
			this.socket.write(this.outBuffer.data, 4);
		} catch (@Pc(39) IOException local39) {
			try {
				this.socket.close();
			} catch (@Pc(45) Exception local45) {
			}
			this.errors++;
			this.response = -2;
			this.socket = null;
		}
	}

	@OriginalMember(owner = "client!jb", name = "b", descriptor = "(I)I")
	private int getPrefetchRequestCount() {
		return this.pendingPrefetchRequests.size() + this.inFlightPrefetchRequests.size();
	}

	@OriginalMember(owner = "client!jb", name = "a", descriptor = "(ZZ)V")
	public final void writeLoggedIn(@OriginalArg(0) boolean arg0) {
		if (this.socket == null) {
			return;
		}
		try {
			this.outBuffer.offset = 0;
			this.outBuffer.p1(arg0 ? 2 : 3);
			this.outBuffer.p3(0);
			this.socket.write(this.outBuffer.data, 4);
		} catch (@Pc(42) IOException local42) {
			try {
				this.socket.close();
			} catch (@Pc(48) Exception local48) {
			}
			this.errors++;
			this.response = -2;
			this.socket = null;
		}
	}

	@OriginalMember(owner = "client!jb", name = "c", descriptor = "(I)V")
	public final void breakConnection() {
		if (this.socket != null) {
			this.socket.breakConnection();
		}
	}

	@OriginalMember(owner = "client!jb", name = "a", descriptor = "(ZLclient!ma;I)V")
	public final void start(@OriginalArg(0) boolean arg0, @OriginalArg(1) BufferedSocket arg1) {
		if (this.socket != null) {
			try {
				this.socket.close();
			} catch (@Pc(14) Exception local14) {
			}
			this.socket = null;
		}
		this.socket = arg1;
		this.method2331();
		this.writeLoggedIn(arg0);
		this.inBuffer.offset = 0;
		this.current = null;
		while (true) {
			@Pc(44) Js5NetRequest currentRequest = (Js5NetRequest) this.inFlightUrgentRequests.removeHead();
			if (currentRequest == null) {
				while (true) {
					currentRequest = (Js5NetRequest) this.inFlightPrefetchRequests.removeHead();
					if (currentRequest == null) {
						if (this.encryptionKey != 0) {
							try {
								this.outBuffer.offset = 0;
								this.outBuffer.p1(4);
								this.outBuffer.p1(this.encryptionKey);
								this.outBuffer.p2(0);
								this.socket.write(this.outBuffer.data, 4);
							} catch (@Pc(107) IOException local107) {
								try {
									this.socket.close();
								} catch (@Pc(113) Exception local113) {
								}
								this.response = -2;
								this.errors++;
								this.socket = null;
							}
						}
						this.latency = 0;
						this.previousLoop = MonotonicClock.currentTimeMillis();
						return;
					}
					this.pendingPrefetchRequests.addTail(currentRequest);
				}
			}
			this.pendingUrgentRequests.addTail(currentRequest);
		}
	}

	@OriginalMember(owner = "client!jb", name = "c", descriptor = "(B)Z")
	public final boolean isUrgentRequestQueueFull() {
		return this.getUrgentRequestCount() >= 20;
	}

	@OriginalMember(owner = "client!jb", name = "d", descriptor = "(B)V")
	public final void rekey() {
		try {
			this.socket.close();
		} catch (@Pc(17) Exception local17) {
		}
		this.response = -1;
		this.encryptionKey = (byte) (Math.random() * 255.0D + 1.0D);
		this.socket = null;
		this.errors++;
	}

	@OriginalMember(owner = "client!jb", name = "d", descriptor = "(I)I")
	public final int getUrgentRequestCount() {
		return this.pendingUrgentRequests.size() + this.inFlightUrgentRequests.size();
	}

	@OriginalMember(owner = "client!jb", name = "b", descriptor = "(Z)V")
	public final void quit() {
		if (this.socket != null) {
			this.socket.close();
		}
	}

	@OriginalMember(owner = "client!jb", name = "a", descriptor = "(IIBIZ)Lclient!pm;")
	public final Js5NetRequest read(@OriginalArg(1) int cacheKeyHigh, @OriginalArg(2) byte trailerLen, @OriginalArg(3) int cacheKeyLow, @OriginalArg(4) boolean isUrgent) {
		@Pc(7) Js5NetRequest readRequest = new Js5NetRequest();
		@Pc(14) long cacheKey = cacheKeyLow + ((long) cacheKeyHigh << 16);
		readRequest.urgent = isUrgent;
		readRequest.secondaryKey = cacheKey;
		readRequest.trailerLen = trailerLen;
		if (isUrgent) {
			if (this.getUrgentRequestCount() >= 20) {
				throw new RuntimeException();
			}
			this.pendingUrgentRequests.addTail(readRequest);
		} else if (this.getPrefetchRequestCount() < 20) {
			this.pendingPrefetchRequests.addTail(readRequest);
		} else {
			throw new RuntimeException();
		}
		return readRequest;
	}

	@OriginalMember(owner = "client!jb", name = "e", descriptor = "(B)V")
	private void method2331() {
		if (this.socket == null) {
			return;
		}
		try {
			this.outBuffer.offset = 0;
			this.outBuffer.p1(6);
			this.outBuffer.p3(3);
			this.socket.write(this.outBuffer.data, 4);
		} catch (@Pc(37) IOException local37) {
			try {
				this.socket.close();
			} catch (@Pc(43) Exception ignored) {
			}
			this.errors++;
			this.socket = null;
			this.response = -2;
		}
	}
}
