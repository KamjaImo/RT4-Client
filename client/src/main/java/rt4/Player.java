package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;
import org.openrs2.deob.annotation.Pc;

@OriginalClass("client!e")
public final class Player extends PathingEntity {

    @OriginalMember(owner = "client!pa", name = "P", descriptor = "Lclient!na;")
    public static JagString password = JagString.EMPTY;
	@OriginalMember(owner = "client!pa", name = "S", descriptor = "Lclient!na;")
	public static JagString usernameInput = JagString.EMPTY;
	@OriginalMember(owner = "client!ba", name = "w", descriptor = "I")
	public static int inTutorialIsland = 0;
	@OriginalMember(owner = "client!ee", name = "b", descriptor = "I")
	public static int level;
    @OriginalMember(owner = "client!bb", name = "E", descriptor = "I")
    public static int runEnergy = 0;
	@OriginalMember(owner = "client!ug", name = "o", descriptor = "I")
	public static int weight = 0;
	@OriginalMember(owner = "client!jl", name = "H", descriptor = "I")
	public static int worldId = -1;
	@OriginalMember(owner = "client!em", name = "B", descriptor = "I")
	public static int rebootTimer = 0;
	@OriginalMember(owner = "client!e", name = "Bc", descriptor = "Lclient!hh;")
	public PlayerAppearance appearance;

	@OriginalMember(owner = "client!e", name = "Mc", descriptor = "Lclient!na;")
	public JagString username;

	@OriginalMember(owner = "client!e", name = "tc", descriptor = "I")
	public int anInt1649 = -1;

	@OriginalMember(owner = "client!e", name = "wc", descriptor = "I")
	private int anInt1651 = 0;

	@OriginalMember(owner = "client!e", name = "uc", descriptor = "I")
	public int anInt1650 = 0;

	@OriginalMember(owner = "client!e", name = "yc", descriptor = "I")
	public int combatLevel = 0;

	@OriginalMember(owner = "client!e", name = "Fc", descriptor = "I")
	public int anInt1656 = 0;

	@OriginalMember(owner = "client!e", name = "Cc", descriptor = "I")
	public int anInt1654 = -1;

	@OriginalMember(owner = "client!e", name = "qc", descriptor = "I")
	public int anInt1648 = -1;

	@OriginalMember(owner = "client!e", name = "Pc", descriptor = "I")
	public int soundRadius = 0;

	@OriginalMember(owner = "client!e", name = "Hc", descriptor = "I")
	public int anInt1658 = -1;

	@OriginalMember(owner = "client!e", name = "Sc", descriptor = "I")
	public int anInt1667 = -1;

	@OriginalMember(owner = "client!e", name = "Oc", descriptor = "Z")
	public boolean aBoolean98 = false;

	@OriginalMember(owner = "client!e", name = "Xc", descriptor = "I")
	public int anInt1671 = 0;

	@OriginalMember(owner = "client!e", name = "Vc", descriptor = "I")
	public int anInt1669 = -1;

	@OriginalMember(owner = "client!e", name = "Wc", descriptor = "I")
	public int anInt1670 = -1;

	@OriginalMember(owner = "client!pa", name = "a", descriptor = "(IIILclient!e;)V")
	public static void animate(@OriginalArg(1) int arg0, @OriginalArg(2) int arg1, @OriginalArg(3) Player arg2) {
		if (arg1 == arg2.seqId && arg1 != -1) {
			@Pc(89) SeqType local89 = SeqTypeList.get(arg1);
			@Pc(92) int local92 = local89.anInt5347;
			if (local92 == 1) {
				arg2.anInt3420 = arg0;
				arg2.anInt3360 = 0;
				arg2.anInt3373 = 1;
				arg2.anInt3425 = 0;
				arg2.anInt3371 = 0;
				SoundPlayer.playSeqSound(arg2.zFine, local89, arg2.xFine, PlayerList.self == arg2, arg2.anInt3425);
			}
			if (local92 == 2) {
				arg2.anInt3371 = 0;
			}
		} else if (arg1 == -1 || arg2.seqId == -1 || SeqTypeList.get(arg1).anInt5355 >= SeqTypeList.get(arg2.seqId).anInt5355) {
			arg2.anInt3373 = 1;
			arg2.anInt3425 = 0;
			arg2.anInt3420 = arg0;
			arg2.anInt3405 = arg2.movementQueueSize;
			arg2.anInt3371 = 0;
			arg2.anInt3360 = 0;
			arg2.seqId = arg1;
			if (arg2.seqId != -1) {
				SoundPlayer.playSeqSound(arg2.zFine, SeqTypeList.get(arg2.seqId), arg2.xFine, arg2 == PlayerList.self, arg2.anInt3425);
			}
		}
	}

	@OriginalMember(owner = "client!ci", name = "a", descriptor = "([I[ILclient!e;B[I)V")
	public static void method865(@OriginalArg(0) int[] arg0, @OriginalArg(1) int[] arg1, @OriginalArg(2) Player arg2, @OriginalArg(4) int[] arg3) {
		for (@Pc(7) int local7 = 0; local7 < arg1.length; local7++) {
			@Pc(20) int local20 = arg1[local7];
			@Pc(24) int local24 = arg3[local7];
			@Pc(28) int local28 = arg0[local7];
			@Pc(30) int local30 = 0;
			while (local24 != 0 && local30 < arg2.aClass147Array3.length) {
				if ((local24 & 0x1) != 0) {
					if (local20 == -1) {
						arg2.aClass147Array3[local30] = null;
					} else {
						@Pc(68) SeqType local68 = SeqTypeList.get(local20);
						@Pc(71) int local71 = local68.anInt5347;
						@Pc(76) Class147 local76 = arg2.aClass147Array3[local30];
						if (local76 != null) {
							if (local20 == local76.anInt5396) {
								if (local71 == 0) {
									local76 = arg2.aClass147Array3[local30] = null;
								} else if (local71 == 1) {
									local76.anInt5400 = 0;
									local76.anInt5398 = 1;
									local76.anInt5399 = 0;
									local76.anInt5408 = local28;
									local76.anInt5404 = 0;
									SoundPlayer.playSeqSound(arg2.zFine, local68, arg2.xFine, arg2 == PlayerList.self, 0);
								} else if (local71 == 2) {
									local76.anInt5400 = 0;
								}
							} else if (local68.anInt5355 >= SeqTypeList.get(local76.anInt5396).anInt5355) {
								local76 = arg2.aClass147Array3[local30] = null;
							}
						}
						if (local76 == null) {
							local76 = arg2.aClass147Array3[local30] = new Class147();
							local76.anInt5396 = local20;
							local76.anInt5398 = 1;
							local76.anInt5404 = 0;
							local76.anInt5408 = local28;
							local76.anInt5399 = 0;
							local76.anInt5400 = 0;
							SoundPlayer.playSeqSound(arg2.zFine, local68, arg2.xFine, arg2 == PlayerList.self, 0);
						}
					}
				}
				local30++;
				local24 >>>= 0x1;
			}
		}
	}

	@OriginalMember(owner = "client!e", name = "c", descriptor = "(B)I")
	@Override
	public final int getSize() {
		return this.appearance == null || this.appearance.anInt2492 == -1 ? super.getSize() : NpcTypeList.get(this.appearance.anInt2492).soze;
	}

	@OriginalMember(owner = "client!e", name = "b", descriptor = "(I)I")
	@Override
	protected final int method2688() {
		return this.anInt3365;
	}

	@OriginalMember(owner = "client!e", name = "a", descriptor = "(ILclient!wa;)V")
	public final void decodeAppearance(@OriginalArg(1) Buffer arg0) {
		arg0.offset = 0;
		@Pc(20) int local20 = arg0.g1();
		@Pc(22) int local22 = -1;
		@Pc(26) int local26 = local20 & 0x1;
		@Pc(37) boolean local37 = (local20 & 0x4) != 0;
		@Pc(41) int local41 = super.getSize();
		@Pc(44) int[] local44 = new int[12];
		this.method2692((local20 >> 3 & 0x7) + 1);
		this.anInt1651 = local20 >> 6 & 0x3;
		this.xFine += (this.getSize() - local41) * 64;
		this.zFine += (this.getSize() - local41) * 64;
		this.anInt1669 = arg0.g1s();
		this.anInt1649 = arg0.g1s();
		this.anInt1650 = 0;
		@Pc(111) int local111;
		@Pc(127) int local127;
		@Pc(134) int local134;
		@Pc(175) int local175;
		for (@Pc(102) int local102 = 0; local102 < 12; local102++) {
			local111 = arg0.g1();
			if (local111 == 0) {
				local44[local102] = 0;
			} else {
				local127 = arg0.g1();
				local134 = (local111 << 8) + local127;
				if (local102 == 0 && local134 == 65535) {
					local22 = arg0.g2();
					this.anInt1650 = arg0.g1();
					break;
				}
				if (local134 >= 32768) {
					local134 = Equipment.anIntArray455[local134 - 32768];
					local44[local102] = local134 | 0x40000000;
					local175 = ObjTypeList.get(local134).team;
					if (local175 != 0) {
						this.anInt1650 = local175;
					}
				} else {
					local44[local102] = local134 - 256 | Integer.MIN_VALUE;
				}
			}
		}
		@Pc(197) int[] local197 = new int[5];
		for (local111 = 0; local111 < 5; local111++) {
			local127 = arg0.g1();
			if (local127 < 0 || local127 >= PlayerAppearance.aShortArrayArray2[local111].length) {
				local127 = 0;
			}
			local197[local111] = local127;
		}
		this.anInt3365 = arg0.g2();
		@Pc(236) long local236 = arg0.g8();
		this.username = Base37.decode37(local236).toTitleCase();
		this.combatLevel = arg0.g1();
		if (local37) {
			this.anInt1671 = arg0.g2();
			this.anInt1656 = this.combatLevel;
			this.anInt1667 = -1;
		} else {
			this.anInt1671 = 0;
			this.anInt1656 = arg0.g1();
			this.anInt1667 = arg0.g1();
			if (this.anInt1667 == 255) {
				this.anInt1667 = -1;
			}
		}
		local134 = this.soundRadius;
		this.soundRadius = arg0.g1();
		if (this.soundRadius == 0) {
			AreaSoundManager.remove(this);
		} else {
			@Pc(309) int local309 = this.anInt1658;
			@Pc(312) int local312 = this.anInt1654;
			@Pc(315) int local315 = this.anInt1670;
			local175 = this.anInt1648;
			this.anInt1648 = arg0.g2();
			this.anInt1658 = arg0.g2();
			this.anInt1654 = arg0.g2();
			this.anInt1670 = arg0.g2();
			if (this.soundRadius != local134 || this.anInt1648 != local175 || this.anInt1658 != local309 || local312 != this.anInt1654 || this.anInt1670 != local315) {
				Static214.method4359(this);
			}
		}
		if (this.appearance == null) {
			this.appearance = new PlayerAppearance();
		}
		local175 = this.appearance.anInt2492;
		this.appearance.method1950(local197, local22, local26 == 1, local44, this.anInt3365);
		if (local175 != local22) {
			this.xFine = this.movementQueueX[0] * 128 + this.getSize() * 64;
			this.zFine = this.movementQueueZ[0] * 128 + this.getSize() * 64;
		}
		if (this.aClass47_Sub1_5 != null) {
			this.aClass47_Sub1_5.method1646();
		}
	}

	@OriginalMember(owner = "client!e", name = "a", descriptor = "(IIIIIIIIJILclient!ga;)V")
	@Override
	public final void method4546(@OriginalArg(0) int arg0, @OriginalArg(1) int arg1, @OriginalArg(2) int arg2, @OriginalArg(3) int arg3, @OriginalArg(4) int arg4, @OriginalArg(5) int arg5, @OriginalArg(6) int arg6, @OriginalArg(7) int arg7, @OriginalArg(8) long arg8, @OriginalArg(9) int arg9, @OriginalArg(10) ParticleSystem arg10) {
		if (this.appearance == null) {
			return;
		}
		@Pc(25) SeqType local25 = this.seqId != -1 && this.anInt3420 == 0 ? SeqTypeList.get(this.seqId) : null;
		@Pc(54) SeqType local54 = this.anInt3366 == -1 || this.aBoolean98 || this.anInt3366 == this.method2681().idleAnimationId && local25 != null ? null : SeqTypeList.get(this.anInt3366);
		@Pc(76) Model local76 = this.appearance.method1954(this.aClass147Array3, this.anInt3373, local54, local25, this.anInt3396, this.anInt3388, this.anInt3360, this.anInt3425, this.anInt3407);
		@Pc(79) int local79 = Static198.method1029();
		if (GlRenderer.enabled && GameShell.maxMemory < 96 && local79 > 50) {
			Static16.method501();
		}
		@Pc(102) int local102;
		if (client.modeWhat != 0 && local79 < 50) {
			local102 = 50 - local79;
			while (Static105.anInt2863 < local102) {
				Static51.aByteArrayArray8[Static105.anInt2863] = new byte[102400];
				Static105.anInt2863++;
			}
			while (Static105.anInt2863 > local102) {
				Static105.anInt2863--;
				Static51.aByteArrayArray8[Static105.anInt2863] = null;
			}
		}
		if (local76 == null) {
			return;
		}
		this.anInt3413 = local76.method4549();
		@Pc(184) Model local184;
		if (Preferences.characterShadowsOn && (this.appearance.anInt2492 == -1 || NpcTypeList.get(this.appearance.anInt2492).shadow)) {
			local184 = ShadowModelList.method1043(160, this.aBoolean171, local54 == null ? local25 : local54, this.xFine, 0, this.zFine, 0, 1, local76, arg0, local54 == null ? this.anInt3425 : this.anInt3407, this.anInt3424, 240);
			if (GlRenderer.enabled) {
				@Pc(188) float local188 = GlRenderer.method4179();
				@Pc(190) float local190 = GlRenderer.method4166();
				GlRenderer.disableDepthMask();
				GlRenderer.method4152(local188, local190 - 150.0F);
				local184.method4546(0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, -1L, arg9, null);
				GlRenderer.enableDepthMask();
				GlRenderer.method4152(local188, local190);
			} else {
				local184.method4546(0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, -1L, arg9, null);
			}
		}
		if (PlayerList.self == this) {
			for (local102 = Static143.aClass102Array1.length - 1; local102 >= 0; local102--) {
				@Pc(245) Class102 local245 = Static143.aClass102Array1[local102];
				if (local245 != null && local245.anInt4052 != -1) {
					@Pc(291) int local291;
					@Pc(302) int local302;
					if (local245.anInt4058 == 1 && local245.anInt4057 >= 0 && NpcList.npcs.length > local245.anInt4057) {
						@Pc(278) Npc local278 = NpcList.npcs[local245.anInt4057];
						if (local278 != null) {
							local291 = local278.xFine / 32 - PlayerList.self.xFine / 32;
							local302 = local278.zFine / 32 - PlayerList.self.zFine / 32;
							this.method1263(null, local302, local76, local291, arg5, arg9, arg0, arg7, arg4, arg3, arg1, local245.anInt4052, arg2, arg6);
						}
					}
					if (local245.anInt4058 == 2) {
						@Pc(340) int local340 = (local245.anInt4053 - Static225.originX) * 4 + 2 - PlayerList.self.xFine / 32;
						local291 = (local245.anInt4046 - Static142.originZ) * 4 + 2 - PlayerList.self.zFine / 32;
						this.method1263(null, local291, local76, local340, arg5, arg9, arg0, arg7, arg4, arg3, arg1, local245.anInt4052, arg2, arg6);
					}
					if (local245.anInt4058 == 10 && local245.anInt4057 >= 0 && PlayerList.players.length > local245.anInt4057) {
						@Pc(395) Player local395 = PlayerList.players[local245.anInt4057];
						if (local395 != null) {
							local291 = local395.xFine / 32 - PlayerList.self.xFine / 32;
							local302 = local395.zFine / 32 - PlayerList.self.zFine / 32;
							this.method1263(null, local302, local76, local291, arg5, arg9, arg0, arg7, arg4, arg3, arg1, local245.anInt4052, arg2, arg6);
						}
					}
				}
			}
		}
		this.method2687(local76);
		this.method2685(local76, arg0);
		local184 = null;
		if (!this.aBoolean98 && this.spotAnimId != -1 && this.anInt3399 != -1) {
			@Pc(471) SpotAnimType local471 = SpotAnimTypeList.get(this.spotAnimId);
			local184 = local471.method1319(this.anInt3418, this.anInt3399, this.anInt3361);
			if (local184 != null) {
				local184.method4575(0, -this.spotAnimY, 0);
				if (local471.aBoolean100) {
					if (Static101.anInt2640 != 0) {
						local184.method4574(Static101.anInt2640);
					}
					if (Static102.anInt2680 != 0) {
						local184.method4564(Static102.anInt2680);
					}
					if (Static62.anInt1938 != 0) {
						local184.method4575(0, Static62.anInt1938, 0);
					}
				}
			}
		}
		@Pc(515) Model local515 = null;
		if (!this.aBoolean98 && this.attachment != null) {
			if (client.loop >= this.anInt3375) {
				this.attachment = null;
			}
			if (this.anInt3390 <= client.loop && this.anInt3375 > client.loop) {
				if (this.attachment instanceof Loc) {
					local515 = (Model) ((Loc) this.attachment).method1049();
				} else {
					local515 = (Model) this.attachment;
				}
				local515.method4575(this.anInt3379 - this.xFine, this.anInt3406 + -this.anInt3424, this.anInt3426 - this.zFine);
				if (this.anInt3400 == 512) {
					local515.method4578();
				} else if (this.anInt3400 == 1024) {
					local515.method4552();
				} else if (this.anInt3400 == 1536) {
					local515.method4563();
				}
			}
		}
		if (GlRenderer.enabled) {
			local76.aBoolean303 = true;
			local76.method4546(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, this.aClass47_Sub1_5);
			if (local184 != null) {
				local184.aBoolean303 = true;
				local184.method4546(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, this.aClass47_Sub1_5);
			}
		} else {
			if (local184 != null) {
				local76 = ((SoftwareModel) local76).method4588(local184);
			}
			if (local515 != null) {
				local76 = ((SoftwareModel) local76).method4588(local515);
			}
			local76.aBoolean303 = true;
			local76.method4546(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, this.aClass47_Sub1_5);
		}
		if (local515 == null) {
			return;
		}
		if (this.anInt3400 == 512) {
			local515.method4563();
		} else if (this.anInt3400 == 1024) {
			local515.method4552();
		} else if (this.anInt3400 == 1536) {
			local515.method4578();
		}
		local515.method4575(this.xFine - this.anInt3379, -this.anInt3406 + this.anInt3424, this.zFine - this.anInt3426);
	}

	@OriginalMember(owner = "client!e", name = "a", descriptor = "(Lclient!ga;ILclient!ak;IIIIIIIIIIII)V")
	private void method1263(@OriginalArg(0) ParticleSystem arg0, @OriginalArg(1) int arg1, @OriginalArg(2) Model arg2, @OriginalArg(3) int arg3, @OriginalArg(4) int arg4, @OriginalArg(5) int arg5, @OriginalArg(7) int arg6, @OriginalArg(8) int arg7, @OriginalArg(9) int arg8, @OriginalArg(10) int arg9, @OriginalArg(11) int arg10, @OriginalArg(12) int arg11, @OriginalArg(13) int arg12, @OriginalArg(14) int arg13) {
		@Pc(12) int local12 = arg3 * arg3 + arg1 * arg1;
		if (local12 < 16 || local12 > 360000) {
			return;
		}
		@Pc(34) int local34 = (int) (Math.atan2((double) arg3, (double) arg1) * 325.949D) & 0x7FF;
		@Pc(46) Model local46 = HintArrowManager.getModel(local34, this.zFine, arg11, this.xFine, arg2, this.anInt3424);
		if (local46 == null) {
			return;
		}
		if (!GlRenderer.enabled) {
			local46.method4546(0, arg10, arg12, arg9, arg8, arg4, arg13, arg7, -1L, arg5, arg0);
			return;
		}
		@Pc(52) float local52 = GlRenderer.method4179();
		@Pc(54) float local54 = GlRenderer.method4166();
		GlRenderer.disableDepthMask();
		GlRenderer.method4152(local52, local54 - 150.0F);
		local46.method4546(0, arg10, arg12, arg9, arg8, arg4, arg13, arg7, -1L, arg5, arg0);
		GlRenderer.enableDepthMask();
		GlRenderer.method4152(local52, local54);
	}

	@OriginalMember(owner = "client!e", name = "a", descriptor = "(B)Z")
	@Override
	public final boolean method2682() {
		return this.appearance != null;
	}

	@OriginalMember(owner = "client!e", name = "e", descriptor = "(I)Lclient!na;")
	public final JagString getName() {
		@Pc(2) JagString local2 = this.username;
		if (Static103.aClass100Array88 != null) {
			local2 = JagString.concatenate(new JagString[] { Static103.aClass100Array88[this.anInt1651], local2 });
		}
		if (Static263.aClass100Array174 != null) {
			local2 = JagString.concatenate(new JagString[] { local2, Static263.aClass100Array174[this.anInt1651] });
		}
		return local2;
	}

	@OriginalMember(owner = "client!e", name = "a", descriptor = "(IIIII)V")
	@Override
	public final void method4545(@OriginalArg(0) int arg0, @OriginalArg(1) int arg1, @OriginalArg(2) int arg2, @OriginalArg(3) int arg3, @OriginalArg(4) int arg4) {
	}

	@OriginalMember(owner = "client!e", name = "a", descriptor = "(BIZI)V")
	public final void teleport(@OriginalArg(1) int arg0, @OriginalArg(2) boolean arg1, @OriginalArg(3) int arg2) {
		super.method2683(this.getSize(), arg0, arg2, arg1);
	}

	@OriginalMember(owner = "client!e", name = "finalize", descriptor = "()V")
	@Override
	public final void finalize() {
	}

	@OriginalMember(owner = "client!e", name = "b", descriptor = "()I")
	@Override
	public final int method4549() {
		return this.anInt3413;
	}
}