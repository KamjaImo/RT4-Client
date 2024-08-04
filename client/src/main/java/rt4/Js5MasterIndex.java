package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;

@OriginalClass("client!al")
public final class Js5MasterIndex {

	@OriginalMember(owner = "client!al", name = "a", descriptor = "Lclient!wa;")
	private Buffer buffer;

	@OriginalMember(owner = "client!al", name = "s", descriptor = "[Lclient!bg;")
	private Js5CachedResourceProvider[] resourceProviders;

	@OriginalMember(owner = "client!al", name = "j", descriptor = "Lclient!k;")
	private final Js5CacheQueue cacheQueue;

	@OriginalMember(owner = "client!al", name = "f", descriptor = "Lclient!jb;")
	private final Js5NetQueue netQueue;

	@OriginalMember(owner = "client!al", name = "c", descriptor = "Lclient!pm;")
	private Js5NetRequest request;

	@OriginalMember(owner = "client!al", name = "<init>", descriptor = "(Lclient!jb;Lclient!k;)V")
	public Js5MasterIndex(@OriginalArg(0) Js5NetQueue arg0, @OriginalArg(1) Js5CacheQueue arg1) {
		this.cacheQueue = arg1;
		this.netQueue = arg0;
		if (!this.netQueue.isUrgentRequestQueueFull()) {
			this.request = this.netQueue.read(255, (byte) 0, 255, true);
		}
	}

	@OriginalMember(owner = "client!al", name = "b", descriptor = "(I)Z")
	public final boolean isReady() {
		if (this.buffer != null) {
			return true;
		}
		if (this.request == null) {
			if (this.netQueue.isUrgentRequestQueueFull()) {
				return false;
			}
			this.request = this.netQueue.read(255, (byte) 0, 255, true);
		}
		if (this.request.incomplete) {
			return false;
		} else {
			this.buffer = new Buffer(this.request.getData());
			this.resourceProviders = new Js5CachedResourceProvider[(this.buffer.data.length - 5) / 8];
			return true;
		}
	}

	@OriginalMember(owner = "client!al", name = "a", descriptor = "(B)V")
	public final void loop() {
		if (this.resourceProviders == null) {
			return;
		}

		// Run a loop on each resource provider.
		@Pc(13) int idx;
		for (idx = 0; idx < this.resourceProviders.length; idx++) {
			if (this.resourceProviders[idx] != null) {
				this.resourceProviders[idx].processPrefetchQueue();
			}
		}
		for (idx = 0; idx < this.resourceProviders.length; idx++) {
			if (this.resourceProviders[idx] != null) {
				this.resourceProviders[idx].loop();
			}
		}
	}

	@OriginalMember(owner = "client!al", name = "a", descriptor = "(IILclient!ge;Lclient!ge;)Lclient!bg;")
	public final Js5CachedResourceProvider getResourceProvider(@OriginalArg(1) int archive, @OriginalArg(2) Cache masterCache, @OriginalArg(3) Cache cache) {
		return this.getResourceProvider(cache, archive, masterCache);
	}

	@OriginalMember(owner = "client!al", name = "a", descriptor = "(Lclient!ge;IIZLclient!ge;)Lclient!bg;")
	private Js5CachedResourceProvider getResourceProvider(@OriginalArg(0) Cache cache, @OriginalArg(2) int archive, @OriginalArg(4) Cache masterCache) {
		if (this.buffer == null) {
			throw new RuntimeException();
		}
		this.buffer.offset = archive * 8 + 5;
		if (this.buffer.data.length <= this.buffer.offset) {
			throw new RuntimeException();
		} else if (this.resourceProviders[archive] == null) {
			@Pc(56) int checksum = this.buffer.g4();
			@Pc(61) int version = this.buffer.g4();
			@Pc(75) Js5CachedResourceProvider resourceProvider = new Js5CachedResourceProvider(archive, cache, masterCache, this.netQueue, this.cacheQueue, checksum, version, true);
			this.resourceProviders[archive] = resourceProvider;
			return resourceProvider;
		} else {
			return this.resourceProviders[archive];
		}
	}
}
