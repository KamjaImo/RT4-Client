package rt4;

import org.openrs2.deob.annotation.OriginalArg;
import org.openrs2.deob.annotation.OriginalClass;
import org.openrs2.deob.annotation.OriginalMember;

// Interface for each of the game's material renderers to implement.
@OriginalClass("client!pc")
public interface MaterialRenderer {

	// Binds the material represented by this material renderer.
	// Subsequent draw operations will be rendered using this material.
	@OriginalMember(owner = "client!pc", name = "b", descriptor = "()V")
	void bind();

	// Unbinds the material represented by this material renderer.
	// This returns drawing to its default state.
	@OriginalMember(owner = "client!pc", name = "a", descriptor = "()V")
	void unbind();

	// Sets the argument for this material renderer.
	// The argument's meaning is implementation-dependent.
	@OriginalMember(owner = "client!pc", name = "a", descriptor = "(I)V")
	void setArgument(@OriginalArg(0) int arg0);

	// Retrieves the flags for this material renderer.
	// 0001 -> Disable textures
	// 0010 -> Disable texture combine RGB mode
	// 0100 -> Disable texture combine alpha mode
	// 1000 -> Disable texture matrix transformations
	@OriginalMember(owner = "client!pc", name = "c", descriptor = "()I")
	int getFlags();
}
