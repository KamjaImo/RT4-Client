package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;

@OriginalClass("client!k")
public final class Js5CacheQueue implements Runnable {

	@OriginalMember(owner = "client!k", name = "q", descriptor = "Lclient!ce;")
	private final SecondaryLinkedList queue = new SecondaryLinkedList();

	@OriginalMember(owner = "client!k", name = "s", descriptor = "I")
	public int size = 0;

	@OriginalMember(owner = "client!k", name = "w", descriptor = "Z")
	private boolean stop = false;

	@OriginalMember(owner = "client!k", name = "v", descriptor = "Ljava/lang/Thread;")
	private Thread thread;

	@OriginalMember(owner = "client!k", name = "<init>", descriptor = "()V")
	public Js5CacheQueue() {
		@Pc(20) PrivilegedRequest request = GameShell.signLink.startThread(5, this);
		while (request.status == 0) {
			ThreadUtils.sleep(10L);
		}
		if (request.status == 2) {
			throw new RuntimeException();
		}
		this.thread = (Thread) request.result;
	}

	@OriginalMember(owner = "client!k", name = "a", descriptor = "(Lclient!c;I)V")
	private void enqueue(@OriginalArg(0) Js5CacheRequest arg0) {
		synchronized (this.queue) {
			this.queue.addTail(arg0);
			this.size++;
			this.queue.notifyAll();
		}
	}

	@OriginalMember(owner = "client!k", name = "a", descriptor = "(I)V")
	public final void quit() {
		this.stop = true;
		synchronized (this.queue) {
			this.queue.notifyAll();
		}
		try {
			this.thread.join();
		} catch (@Pc(23) InterruptedException ignored) {
		}
		this.thread = null;
	}

	@OriginalMember(owner = "client!k", name = "a", descriptor = "(Lclient!ge;I[BI)Lclient!c;")
	public final Js5CacheRequest write(@OriginalArg(0) Cache arg0, @OriginalArg(2) byte[] arg1, @OriginalArg(3) int arg2) {
		@Pc(7) Js5CacheRequest request = new Js5CacheRequest();
		request.data = arg1;
		request.urgent = false;
		request.secondaryKey = arg2;
		request.cache = arg0;
		request.type = Js5CacheRequest.Type.Write;
		this.enqueue(request);
		return request;
	}

	@OriginalMember(owner = "client!k", name = "a", descriptor = "(IILclient!ge;)Lclient!c;")
	public final Js5CacheRequest read(@OriginalArg(0) int arg0, @OriginalArg(2) Cache arg1) {
		@Pc(7) Js5CacheRequest local7 = new Js5CacheRequest();
		local7.cache = arg1;
		local7.type = Js5CacheRequest.Type.Read;
		local7.urgent = false;
		local7.secondaryKey = arg0;
		this.enqueue(local7);
		return local7;
	}

	@OriginalMember(owner = "client!k", name = "a", descriptor = "(Lclient!ge;BI)Lclient!c;")
	public final Js5CacheRequest readSynchronous(@OriginalArg(0) Cache arg0, @OriginalArg(2) int arg1) {
		@Pc(9) Js5CacheRequest local9 = new Js5CacheRequest();
		local9.type = Js5CacheRequest.Type.ReadSynchronous;
		synchronized (this.queue) {
			@Pc(31) Js5CacheRequest local31 = (Js5CacheRequest) this.queue.head();
			while (true) {
				if (local31 == null) {
					break;
				}
				if (local31.secondaryKey == (long) arg1 && local31.cache == arg0 && local31.type == 2) {
					local9.data = local31.data;
					local9.incomplete = false;
					return local9;
				}
				local31 = (Js5CacheRequest) this.queue.next();
			}
		}
		local9.data = arg0.read(arg1);
		local9.incomplete = false;
		local9.urgent = true;
		return local9;
	}

	@OriginalMember(owner = "client!k", name = "run", descriptor = "()V")
	@Override
	public final void run() {
		while (!this.stop) {
			// Pull the next request in the queue
			@Pc(19) Js5CacheRequest currentRequest;
			synchronized (this.queue) {
				currentRequest = (Js5CacheRequest) this.queue.removeHead();

				// If queue is empty, wait
				if (currentRequest == null) {
					try {
						this.queue.wait();
					} catch (@Pc(35) InterruptedException ignored) {
					}
					continue;
				}
				this.size--;
			}
			try {
				// Read from or write to cache, depending on request type
				if (currentRequest.type == Js5CacheRequest.Type.Write) {
					currentRequest.cache.write((int) currentRequest.secondaryKey, currentRequest.data.length, currentRequest.data);
				} else if (currentRequest.type == Js5CacheRequest.Type.Read) {
					currentRequest.data = currentRequest.cache.read((int) currentRequest.secondaryKey);
				}
			} catch (@Pc(83) Exception local83) {
				TracingException.report(null, local83);
			}
			currentRequest.incomplete = false;
		}
	}
}
