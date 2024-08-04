package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;

@OriginalClass("client!ii")
public final class Js5Index {

	@OriginalMember(owner = "client!ii", name = "b", descriptor = "[[I")
	public int[][] fileNameHashes;

	@OriginalMember(owner = "client!ii", name = "d", descriptor = "[I")
	public int[] groupChecksums;

	@OriginalMember(owner = "client!ii", name = "f", descriptor = "I")
	public int size;

	@OriginalMember(owner = "client!ii", name = "h", descriptor = "Lclient!jg;")
	public IntHashTable groupNameHashTable;

	@OriginalMember(owner = "client!ii", name = "m", descriptor = "[I")
	public int[] groupIds;

	@OriginalMember(owner = "client!ii", name = "n", descriptor = "[I")
	public int[] groupCapacities;

	@OriginalMember(owner = "client!ii", name = "o", descriptor = "[I")
	public int[] groupNameHashes;

	@OriginalMember(owner = "client!ii", name = "p", descriptor = "[I")
	public int[] groupSizes;

	@OriginalMember(owner = "client!ii", name = "r", descriptor = "[I")
	public int[] groupVersions;

	@OriginalMember(owner = "client!ii", name = "s", descriptor = "[[I")
	public int[][] fileIds;

	@OriginalMember(owner = "client!ii", name = "u", descriptor = "I")
	public int capacity;

	@OriginalMember(owner = "client!ii", name = "v", descriptor = "I")
	public int version;

	@OriginalMember(owner = "client!ii", name = "x", descriptor = "[Lclient!jg;")
	public IntHashTable[] fileNameHashTables;

	@OriginalMember(owner = "client!ii", name = "z", descriptor = "I")
	public final int checksum;

	@OriginalMember(owner = "client!ii", name = "<init>", descriptor = "([BI)V")
	public Js5Index(@OriginalArg(0) byte[] rawIndexData, @OriginalArg(1) int checksum) {
		this.checksum = Buffer.crc32(rawIndexData, rawIndexData.length);
		if (checksum != this.checksum) {
			throw new RuntimeException();
		}
		this.parseRawIndexData(rawIndexData);
	}

	@OriginalMember(owner = "client!ii", name = "a", descriptor = "(I[B)V")
	private void parseRawIndexData(@OriginalArg(1) byte[] rawIndexData) {
		@Pc(12) Buffer uncompressedIndexData = new Buffer(Js5Compression.uncompress(rawIndexData));
		@Pc(16) int majorVersion = uncompressedIndexData.g1();
		if (majorVersion != 5 && majorVersion != 6) {
			throw new RuntimeException();
		}
		if (majorVersion >= 6) {
			this.version = uncompressedIndexData.g4();
		} else {
			this.version = 0;
		}
		@Pc(48) int containsHashes = uncompressedIndexData.g1();
		@Pc(50) int currentGroupId = 0;
		this.size = uncompressedIndexData.g2();
		@Pc(59) int highestGroupId = -1;
		this.groupIds = new int[this.size];
		@Pc(66) int i;
		for (i = 0; i < this.size; i++) {
			this.groupIds[i] = currentGroupId += uncompressedIndexData.g2();
			if (this.groupIds[i] > highestGroupId) {
				highestGroupId = this.groupIds[i];
			}
		}
		this.capacity = highestGroupId + 1;
		this.groupVersions = new int[this.capacity];
		this.fileIds = new int[this.capacity][];
		this.groupChecksums = new int[this.capacity];
		this.groupCapacities = new int[this.capacity];
		this.groupSizes = new int[this.capacity];
		if (containsHashes != 0) {
			this.groupNameHashes = new int[this.capacity];
			for (i = 0; i < this.capacity; i++) {
				this.groupNameHashes[i] = -1;
			}
			for (i = 0; i < this.size; i++) {
				this.groupNameHashes[this.groupIds[i]] = uncompressedIndexData.g4();
			}
			this.groupNameHashTable = new IntHashTable(this.groupNameHashes);
		}
		for (i = 0; i < this.size; i++) {
			this.groupChecksums[this.groupIds[i]] = uncompressedIndexData.g4();
		}
		for (i = 0; i < this.size; i++) {
			this.groupVersions[this.groupIds[i]] = uncompressedIndexData.g4();
		}
		for (i = 0; i < this.size; i++) {
			this.groupSizes[this.groupIds[i]] = uncompressedIndexData.g2();
		}
		// @Pc(273) int local273;
		@Pc(278) int currentGroupSize;
		@Pc(280) int highestFileId;
		@Pc(288) int j;
		for (i = 0; i < this.size; i++) {
			@Pc(50) int local50 = 0;
			currentGroupId = this.groupIds[i];
			currentGroupSize = this.groupSizes[currentGroupId];
			highestFileId = -1;
			this.fileIds[currentGroupId] = new int[currentGroupSize];
			for (j = 0; j < currentGroupSize; j++) {
				@Pc(306) int currentFileId = this.fileIds[currentGroupId][j] = local50 += uncompressedIndexData.g2();
				if (currentFileId > highestFileId) {
					highestFileId = currentFileId;
				}
			}
			this.groupCapacities[currentGroupId] = highestFileId + 1;
			if (highestFileId + 1 == currentGroupSize) {
				this.fileIds[currentGroupId] = null;
			}
		}
		if (containsHashes == 0) {
			return;
		}
		this.fileNameHashTables = new IntHashTable[highestGroupId + 1];
		this.fileNameHashes = new int[highestGroupId + 1][];
		for (i = 0; i < this.size; i++) {
			currentGroupId = this.groupIds[i];
			currentGroupSize = this.groupSizes[currentGroupId];
			this.fileNameHashes[currentGroupId] = new int[this.groupCapacities[currentGroupId]];
			for (highestFileId = 0; highestFileId < this.groupCapacities[currentGroupId]; highestFileId++) {
				this.fileNameHashes[currentGroupId][highestFileId] = -1;
			}
			for (highestFileId = 0; highestFileId < currentGroupSize; highestFileId++) {
				if (this.fileIds[currentGroupId] == null) {
					j = highestFileId;
				} else {
					j = this.fileIds[currentGroupId][highestFileId];
				}
				this.fileNameHashes[currentGroupId][j] = uncompressedIndexData.g4();
			}
			this.fileNameHashTables[currentGroupId] = new IntHashTable(this.fileNameHashes[currentGroupId]);
		}
	}
}
